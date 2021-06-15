package schoolbot.util;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.StateMachine;
import schoolbot.objects.school.Classroom;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Checks
{
      private static final Logger LOGGER = LoggerFactory.getLogger(Checks.class);

      public static boolean isNumber(String number)
      {
            return number.matches("-?\\d+(\\.\\d+)?");
      }

      public static int isNum(String number)
      {
            try
            {
                  return Integer.parseInt(number);
            }
            catch (Exception e)
            {
                  return -1;
            }
      }

      public static boolean allMatchesNumber(String message)
      {
            return message.chars().allMatch(Character::isDigit);
      }

      public static boolean isValidEmail(String potentialEmail)
      {
            Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.(?:[A-Z]{2}|com|org|edu|net|)$\n");
            Matcher matcher = pattern.matcher(potentialEmail);
            return matcher.matches();
      }

      public static boolean between(int i, int maxValueInclusive)
      {
            return (i >= 1 && i <= maxValueInclusive);
      }

      public static boolean isValidAssignmentDate(String potDate, Classroom classroom)
      {
            if (!potDate.contains("/")) return false;

            LocalDate ld;

            try
            {

                  ld = LocalDate.parse(potDate, DateTimeFormatter.ofPattern("M/d/yyyy"));
                  return ld.isAfter(LocalDate.now()) || ld.isEqual(LocalDate.now());
                  // return ld.isAfter(classroom.getClassStartDate()) && ld.isBefore(classroom.getClassEndDate()); commented out because other things arent implemented yet
            }
            catch (Exception e)
            {
                  LOGGER.error("Error has occurred whilst parsing assignment date", e);
                  return false;
            }
      }

      public static LocalDate checkValidDate(String potentialDate)
      {

            try
            {
                  return LocalDate.parse(potentialDate, DateTimeFormatter.ofPattern("M/d/yyyy"));
            }
            catch (Exception e)
            {
                  return null;
            }
      }

      public static boolean checkValidTime(String time)
      {
            if (!time.contains(":")) return false;
            if (!(time.toLowerCase().contains("pm") || time.toLowerCase().contains("am"))) return false;
            if (time.toLowerCase().contains("am") && time.toLowerCase().contains("pm")) return false;
            if (time.split(":").length != 2) return false;
            return isNumber(time.replaceAll(":", "")
                    .replaceAll(time.toLowerCase().contains("am") ? "am" : "pm", ""));
      }

      public static LocalDateTime validTime(GuildMessageReceivedEvent event, LocalDate date)
      {
            String time = event.getMessage().getContentRaw();

            if (!time.contains(":")) return null;
            if (!(time.toLowerCase().contains("pm") || time.toLowerCase().contains("am"))) return null;
            if (time.toLowerCase().contains("am") && time.toLowerCase().contains("pm")) return null;
            if (time.split(":").length != 2) return null;
            if (!isNumber(time.replaceAll(":", "")
                    .replaceAll(time.toLowerCase().contains("am") ? "am" : "pm", ""))) return null;

            String[] t = time.split(":");


            if (time.toLowerCase().contains("am"))
            {
                  int hour = Integer.parseInt(t[0]);
                  int minute = Integer.parseInt(t[1].replaceAll("am", ""));

                  return LocalDateTime.of(date, LocalTime.of(hour, minute));
            }
            else
            {
                  int hour = Integer.parseInt(t[0]);
                  int minute = Integer.parseInt(t[1].replaceAll("pm", ""));

                  if (hour == 12)
                  {
                        return null;
                  }

                  return LocalDateTime.of(date, LocalTime.of((12 + hour), minute));
            }
      }

      public static Classroom messageSentFromClassChannel(CommandEvent event)
      {
            Schoolbot schoolbot = event.getSchoolbot();
            List<Classroom> classroomList = event.getGuildClasses();

            List<Long> classChannels = classroomList
                    .stream()
                    .map(Classroom::getChannelID)
                    .collect(Collectors.toList());

            long textChanel = event.getTextChannel().getIdLong();


            if (classChannels.contains(textChanel))
            {
                  // Get class room

                  return classroomList
                          .stream()
                          .filter(clazzroom -> clazzroom.getChannelID() == textChanel)
                          .findFirst()
                          .orElseThrow(() -> new IllegalStateException("Class does not exist"));
            }
            return null;
      }

      /**
       * Returns if the event contains the same user and channel as the base event
       * The event argument is the event fired when a message is sent in a guild.
       * The IDs field are expected to be channelIDs and a userID
       * The machine field is the current state machine that we are checking
       *
       * @param event   GuildMessageReceivedEvent anticipated to be respond event
       * @param machine The state machine that we are using
       * @param ids     Channel ID, User ID (in that exact order)
       * @return If the event contains same user and channel as base event false otherwise
       */
      public static <S extends StateMachine> boolean eventMeetsPrerequisites(@NotNull GuildMessageReceivedEvent event, S machine, Long... ids)
      {
            var jda = event.getJDA();
            var channel = event.getChannel();


            if (ids.length != 2)
            {
                  channel.sendMessage("This command has been programmed incorrectly. Please contact damon#9999").queue();
                  LOGGER.warn("You passed too many or not enough IDs through this function!");
                  jda.removeEventListener(machine);
                  return false;
            }


            long channelId = ids[0];
            long userId = ids[1];
            String message = event.getMessage().getContentRaw().toLowerCase();


            var sameUser = event.getChannel().getIdLong() == channelId
                           && event.getAuthor().getIdLong() == userId;


            if (!sameUser)
            {
                  return false;
            }

            if (message.equalsIgnoreCase("stop") || message.equalsIgnoreCase("exit"))
            {
                  channel.sendMessage("I will now abort. Call the command to try again!").queue();
                  jda.removeEventListener(machine);
                  return false;
            }

            return true;
      }

      public static List<Long> validRoleCheck(CommandEvent event)
      {

            List<Long> classRoles = event.getGuildClasses()
                    .stream()
                    .map(Classroom::getRoleID)
                    .collect(Collectors.toList());

            return event.getMember()
                    .getRoles()
                    .stream()
                    .map(Role::getIdLong)
                    .filter(validRoles -> Collections.frequency(classRoles, validRoles) > 1)
                    .collect(Collectors.toList());
      }


}
