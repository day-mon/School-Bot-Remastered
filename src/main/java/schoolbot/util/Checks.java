package schoolbot.util;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
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

      private Checks() {};

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
            catch (NumberFormatException e)
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

      public static LocalDate isValidAssignmentDate(StateMachineValues values)
      {
            var potDate = values.getMessageReceivedEvent().getMessage().getContentRaw();
            var classroom = values.getClassroom();
            LocalDate ld;


            if (potDate.equalsIgnoreCase("tomorrow"))
            {
                  ld = LocalDate.now().plusDays(1);
            }
            else if (potDate.equalsIgnoreCase("today"))
            {
                  ld = LocalDate.now();
            }
            else
            {
                  try
                  {
                        ld = LocalDate.parse(potDate, DateTimeFormatter.ofPattern("M/d/yyyy"));
                  }
                  catch (RuntimeException e)
                  {
                        LOGGER.error("Error occurred while checking assignment time", e);
                        return null;
                  }
            }


            if (ld.isAfter(classroom.getStartDate()) && ld.isBefore(classroom.getEndDate()))
            {
                  return ld;
            }

            return null;


      }

      public static LocalDate checkValidDate(String potentialDate)
      {

            try
            {
                  return LocalDate.parse(potentialDate, DateTimeFormatter.ofPattern("M/d/yyyy"));
            }
            catch (RuntimeException e)
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

      public static LocalDateTime validTime(StateMachineValues values, LocalDate date)
      {
            var event = values.getMessageReceivedEvent();
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
                  int minute = Integer.parseInt(t[1].toLowerCase().replaceAll("am", ""));

                  var returnVal = LocalDateTime.of(date, LocalTime.of(hour, minute));
                  return returnVal.isAfter(LocalDateTime.now()) ? returnVal : null;
            }
            else
            {
                  int hour = Integer.parseInt(t[0]);
                  int minute = Integer.parseInt(t[1].toLowerCase().replaceAll("pm", ""));

                  if (hour == 12)
                  {
                        hour = -12;
                  }

                  var returnVal = LocalDateTime.of(date, LocalTime.of(hour + 12, minute));

                  return returnVal.isAfter(LocalDateTime.now()) ? returnVal : null;
            }
      }

      public static Classroom messageSentFromClassChannel(CommandEvent event)
      {
            List<Classroom> classroomList = event.getGuildClasses();
            long textChanel = event.getTextChannel().getIdLong();


            // Get class room

            return classroomList
                    .stream()
                    .filter(clazzroom -> clazzroom.getChannelID() == textChanel)
                    .findFirst()
                    .orElse(null);

      }

      /**
       * <ul>
       * Returns a classroom if found <b>NULL</b> if otherwise.
       * The values parameter is the StateMachine Values.. (I dont know what else to say tbh)
       * </ul>
       *
       *
       * <p>
       * This method takes in values and evaluates if the message that was just sent comes from a channel
       * that is associated with a class. If it is it will return the classroom and set the classroom in the values.
       * Otherwise the method will return <b>NULL</b>;
       * </p>
       *
       * @param values StateMachineValues passed through to set classroom if possible
       * @return Classroom if found, <b>NULL</b> if otherwise
       */
      public static Classroom messageSentFromClassChannel(StateMachineValues values)
      {
            var commandEvent = values.getCommandEvent();
            var classroomList = commandEvent.getGuildClasses();

            long textChanel = commandEvent.getTextChannel().getIdLong();


            return classroomList.stream()
                    .filter(clazzroom -> clazzroom.getChannelID() == textChanel)
                    .limit(1)
                    .peek(values::setClassroom)
                    .findAny()
                    .orElse(null);

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


      /**
       * Returns if the event contains the same user and channel as the base event
       *
       * @param values GuildMessageReceivedEvent anticipated to be respond event
       * @return If the event contains same user and channel as base event false otherwise
       */
      public static <S extends StateMachine> boolean eventMeetsPrerequisites(@NotNull StateMachineValues values)
      {
            var commandEvent = values.getCommandEvent();
            var event = values.getMessageReceivedEvent();
            var jda = values.getJda();
            var machine = values.getMachine();


            long channelId = event.getChannel().getIdLong();
            long userId = event.getAuthor().getIdLong();
            String message = values.getMessageReceivedEvent().getMessage().getContentRaw().toLowerCase();


            boolean sameUser = commandEvent.getUser().getIdLong() == userId
                               && commandEvent.getTextChannel().getIdLong() == channelId;


            if (!sameUser)
            {
                  return false;
            }

            if (message.equalsIgnoreCase("stop") || message.equalsIgnoreCase("exit"))
            {
                  EmbedUtils.warn(event, "I will now abort. Call the command to try again!");
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
