package schoolbot.util;

import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.Classroom;

import java.time.LocalDate;
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

            LocalDate ld = null;

            try
            {

                  ld = LocalDate.parse(potDate, DateTimeFormatter.ofPattern("M/d/yyyy"));
                  return ld.isAfter(LocalDate.now()) || ld.isEqual(LocalDate.now());
                  // return ld.isAfter(classroom.getClassStartDate()) && ld.isBefore(classroom.getClassEndDate()); commented out because other things arent implemented yet
            }
            catch (Exception e)
            {
                  e.printStackTrace();
                  return false;
            }
      }

      public static boolean checkValidTime(String time)
      {
            if (!time.contains(":")) return false;
            if (!(time.toLowerCase().contains("pm") || time.toLowerCase().contains("am"))) return false;
            if (time.toLowerCase().contains("am") && time.toLowerCase().contains("pm")) return false;
            if (time.split(":").length != 2) return false;
            return isNumber(time.replaceAll(":", "")
                    .replaceAll(time.contains("am") ? "am" : "pm", ""));
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
                  Classroom classroom = classroomList
                          .stream()
                          .filter(clazzroom -> clazzroom.getChannelID() == textChanel)
                          .findFirst()
                          .orElseThrow(() -> new IllegalStateException("Class does not exist"));


                  return classroom;
            }

            return null;
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
                    .filter(validRolez -> Collections.frequency(classRoles, validRolez) > 1)
                    .collect(Collectors.toList());
      }


}
