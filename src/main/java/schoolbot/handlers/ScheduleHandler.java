package schoolbot.handlers;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.misc.Reminder;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.util.DatabaseUtils;
import schoolbot.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleHandler
{
      private static final ScheduledExecutorService reminderExecutor = Executors.newScheduledThreadPool(10, runnable -> new Thread(runnable, "SchoolBot Scheduler-Thread"));
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final Schoolbot schoolbot;

      public ScheduleHandler(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
            runAssignmentsReminder();
            runClassReminder();
            runStatusSwitcher();
      }

      public void runAssignmentsReminder()
      {
            reminderExecutor.scheduleAtFixedRate(() ->
            {
                  List<Assignment> assignments = DatabaseUtils.checkRemindTimes(schoolbot);
                  assignments.forEach(this::sendAssignmentAlert);
            }, 0, 10, TimeUnit.SECONDS);

      }

      public void runClassReminder()
      {
            reminderExecutor.scheduleAtFixedRate(() ->
            {
                  List<Reminder> reminderList = DatabaseUtils.classReminder(schoolbot);
                  reminderList.forEach(this::sendClassroomAlert);
            }, 0, 10, TimeUnit.SECONDS);
      }

      public void runStatusSwitcher()
      {
            var jda = schoolbot.getJda();

            List<Activity> activityList = List.of(
                    Activity.watching("mark sleep"),
                    Activity.streaming("warner growing", "https://www.youtube.com/watch?v=PLOPygVcaVE"),
                    Activity.watching("damon bench joesphs weight"),
                    Activity.streaming("chakara balancing seminar", "https://www.youtube.com/watch?v=vqklftk89Nw")
            );

            reminderExecutor.scheduleAtFixedRate(() ->
                    jda.getPresence().setPresence(OnlineStatus.ONLINE, activityList.get(new Random().nextInt(activityList.size()))), 0, 5, TimeUnit.MINUTES);
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

                  var due = Duration.between(LocalDateTime.now(), assignment.getDueDate());

                  String mention = role != null ? role.getAsMention() : "Students of " + classroom.getName();
                  String dueMessage = (due.toMinutes() <= 0) ?
                          String.format("%s, ** %s ** is **now due**", mention, assignment.getName())
                          :
                          String.format("%s, ** %s ** is due in **%d days**, **%d hours**, **%d minutes**, and **%d seconds.**", mention, assignment.getName(), due.toDaysPart(), due.toHoursPart(), due.toMinutesPart(), due.toSecondsPart());

                  /*
                   * This will check if the assignment is pass due by one minute and 10 seconds
                   * If it is past this time this means that either
                   * A. There was a heartbeat issue where the bot was offline for 10 seconds after the initial due time
                   * or
                   * B. The bot was just offline and couldn't alert
                   */
                  var secondsBetween = Duration.between(assignment.getDueDate().toLocalTime(), LocalDateTime.now()).getSeconds();
                  boolean overDueCheck = secondsBetween > 70;

                  if (overDueCheck)
                  {
                        dueMessage = String.format("%s, ** %s ** at ** %s ** but we could not alert you due to some unfortunate down time. I am working to improve.", mention, assignment.getName(), StringUtils.formatDate(assignment.getDueDate()));
                        LOGGER.warn("Overdue by: {} seconds", secondsBetween);
                  }


                  LOGGER.info("{} has been notified", classroom.getName());
                  channel.sendMessage(dueMessage).queue();

                  if (due.toMinutes() <= 0)
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
                  var due = Duration.between(LocalTime.now(), classroom.getStartDateWithTime().toLocalTime()).toMinutes();
                  var channel = schoolbot.getJda().getTextChannelById(classroom.getChannelID());
                  var role = schoolbot.getJda().getRoleById(classroom.getRoleID());

                  if (channel == null)
                  {
                        LOGGER.error("{} has no channel ID", classroom.getName());
                        return;
                  }


                  String mention = role != null ? role.getAsMention() : "Students of " + classroom.getName();
                  String dueMessage = (due <= 0) ?
                          String.format("%s, ** %s ** is **now starting**", mention, classroom.getName())
                          :
                          String.format("%s, ** %s ** is starting in ** %d ** minutes", mention, classroom.getName(), due + 1);

                  LOGGER.info("Due time: {} min", due);


                  /*
                   * This will check if the assignment is pass due by one minute and 10 seconds
                   * If it is past this time this means that either
                   * A. There was a heartbeat issue where the bot was offline for 10 seconds after the initial due time
                   * or
                   * B. The bot was just offline and couldn't alert
                   */
                  var secondsBetween = Duration.between(classroom.getStartDateWithTime().toLocalTime(), LocalDateTime.now()).getSeconds();
                  boolean overDueCheck = secondsBetween > 70;

                  if (overDueCheck)
                  {
                        dueMessage = String.format("%s, ** %s ** was due at ** %s ** but we could not alert you due to some unfortunate down time. I am working to improve", mention, classroom.getName(), StringUtils.formatDate(classroom.getStartDateWithTime()));
                        LOGGER.warn("Overdue by: {} seconds", secondsBetween);
                  }


                  LOGGER.info("{} has been notified", classroom.getName());
                  channel.sendMessage(dueMessage).queue();

                  DatabaseUtils.removeReminder(schoolbot, reminder);

                  var lastReminder = DatabaseUtils.lastClassReminder(schoolbot, reminder);

                  if (lastReminder)
                  {
                        schoolbot.getWrapperHandler().removeClassroom(classroom, schoolbot);
                  }

            }
            catch (Exception e)
            {
                  LOGGER.error("Unexpected Error has occurred", e);
            }
      }
}
