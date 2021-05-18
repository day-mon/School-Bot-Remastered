package schoolbot.handlers;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.util.DatabaseUtil;

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
      }

      public void runAssignmentsReminder()
      {
            reminderExecutor.scheduleAtFixedRate(() ->
            {
                  List<Assignment> assignments = DatabaseUtil.checkRemindTimes(schoolbot);
                  assignments.forEach(this::sendAssignmentAlert);
            }, 0, 10, TimeUnit.SECONDS);
      }


      private void sendAssignmentAlert(Assignment assignment)
      {
            try
            {
                  Classroom classroom = assignment.getClassroom();
                  TextChannel channel = schoolbot.getJda().getTextChannelById(classroom.getChannelID());
                  Role role = schoolbot.getJda().getRoleById(classroom.getRoleID());

                  if (channel == null)
                  {

                        LOGGER.error("{} has no channel ID", assignment.getName());
                  }
                  else
                  {
                        String mention = role == null ? role.getAsMention() : "Students of " + classroom.getClassName();

                        LOGGER.info("{} has been notified", classroom.getClassName());
                        channel.sendMessageFormat("%s, ** %s ** is due in ** %s ** minutes", mention, assignment.getName(), (assignment.getDueDate().toLocalDateTime().minusMinutes(LocalDateTime.now().getMinute()).getMinute())).queue();
                  }
            }
            catch (Exception e)
            {
                  LOGGER.error("Unexpected Error has occurred", e);
            }
      }
}