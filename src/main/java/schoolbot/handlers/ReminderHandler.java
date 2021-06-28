package schoolbot.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.misc.Reminder;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.util.DatabaseUtil;
import schoolbot.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderHandler
{
      private static final ScheduledExecutorService reminderExecutor = Executors.newScheduledThreadPool(10, runnable -> new Thread(runnable, "SchoolBot Reminder-Thread"));
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final Schoolbot schoolbot;

      public ReminderHandler(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
            runAssignmentsReminder();
            runClassReminder();
      }

      public void runAssignmentsReminder()
      {

            // todo: fix this reminders arent working!

            reminderExecutor.scheduleAtFixedRate(() ->
            {
                  List<Assignment> assignments = DatabaseUtil.checkRemindTimes(schoolbot);
                  assignments.forEach(this::sendAssignmentAlert);
            }, 0, 10, TimeUnit.SECONDS);

      }

      public void runClassReminder()
      {
            reminderExecutor.scheduleAtFixedRate(() ->
            {
                  List<Reminder> reminderList = DatabaseUtil.classReminder(schoolbot);
                  reminderList.forEach(this::sendClassroomAlert);
            }, 0, 10, TimeUnit.SECONDS);
      }


      private void sendAssignmentAlert(Assignment assignment)
      {
            try
            {
                  var classroom = assignment.getClassroom();
                  var channel = schoolbot.getJda().getTextChannelById(classroom.getChannelID());
                  var role = schoolbot.getJda().getRoleById(classroom.getRoleID());


                  if (channel == null)
                  {
                        LOGGER.error("{} has no channel ID", assignment.getName());
                        return;
                  }


                  int due = assignment.getDueDate().minusMinutes(LocalDateTime.now().getMinute()).getMinute();
                  String mention = role != null ? role.getAsMention() : "Students of " + classroom.getName();
                  String dueMessage = (due <= 0) ?
                          String.format("%s, ** %s ** is **now due**", mention, assignment.getName())
                          :
                          String.format("%s, ** %s ** is due in ** %d ** minutes", mention, assignment.getName(), due);

                  /*
                   * This will check if the assignment is pass due by one minute and 10 seconds
                   * If it is past this time this means that either
                   * A. There was a heartbeat issue where the bot was offline for 10 seconds after the initial due time
                   * or
                   * B. The bot was just offline and couldn't alert
                   */
                  boolean overDueCheck = Duration.between(assignment.getDueDate(), LocalDateTime.now()).getSeconds() > 70;

                  if (overDueCheck)
                  {
                        dueMessage = String.format("%s, ** %s ** at ** %s ** but we could not alert you due to some unfortunate down time. I am working to improve.", mention, assignment.getName(), StringUtils.formatDate(assignment.getDueDate()));
                  }


                  LOGGER.info("{} has been notified", classroom.getName());
                  channel.sendMessage(dueMessage).queue();

                  if (due <= 0)
                  {
                        var guildId = channel.getGuild().getIdLong();

                        schoolbot.getWrapperHandler().removeAssignment(guildId, assignment);
                        LOGGER.debug("Assignment list size {}", classroom.getAssignments().size());
                  }


            }
            catch (Exception e)
            {
                  LOGGER.error("Unexpected Error has occurred", e);
            }
      }


      private void sendClassroomAlert(Reminder reminder)
      {
            try
            {
                  var classroom = (Classroom) reminder.obj();
                  var channel = schoolbot.getJda().getTextChannelById(classroom.getChannelID());
                  var role = schoolbot.getJda().getRoleById(classroom.getRoleID());

                  if (channel == null)
                  {
                        LOGGER.error("{} has no channel ID", classroom.getName());
                        return;
                  }

                  int due = classroom.getStartDateWithTime().minusMinutes(LocalDateTime.now().getMinute()).getMinute();
                  String mention = role != null ? role.getAsMention() : "Students of " + classroom.getName();
                  String dueMessage = (due <= 0) ?
                          String.format("%s, ** %s ** is **now starting**", mention, classroom.getName())
                          :
                          String.format("%s, ** %s ** is starts in ** %d ** minutes", mention, classroom.getName(), due);

                  /*
                   * This will check if the assignment is pass due by one minute and 10 seconds
                   * If it is past this time this means that either
                   * A. There was a heartbeat issue where the bot was offline for 10 seconds after the initial due time
                   * or
                   * B. The bot was just offline and couldn't alert
                   */
                  boolean overDueCheck = Duration.between(classroom.getStartDateWithTime(), LocalDateTime.now()).getSeconds() > 70;

                  if (overDueCheck)
                  {
                        dueMessage = String.format("%s, ** %s ** was due at ** %s ** but we could not alert you due to some unfortunate down time. I am working to improve", mention, classroom.getName(), StringUtils.formatDate(classroom.getStartDateWithTime()));

                  }


                  LOGGER.info("{} has been notified", classroom.getName());
                  channel.sendMessage(dueMessage).queue();

                  DatabaseUtil.removeReminder(schoolbot, reminder);

                  var lastReminder = DatabaseUtil.lastClassReminder(schoolbot, reminder);

                  if (lastReminder)
                  {
                        System.out.println("last one");
                  }
                  else
                  {
                        System.out.println("not last one");
                  }

            }
            catch (Exception e)
            {
                  LOGGER.error("Unexpected Error has occurred", e);
            }
      }
}
