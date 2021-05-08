package schoolbot.natives.util;

import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.natives.objects.school.Classroom;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


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
            return potentialEmail.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.(?:[A-Z]{2}|com|org|edu|net|)$\n");
      }

      public static boolean between(int i, int minValueInclusive, int maxValueInclusive)
      {
            return (i >= minValueInclusive && i <= maxValueInclusive);
      }

      public static boolean isValidAssignmentDate(String potDate, Classroom classroom)
      {
            if (!potDate.contains("/")) return false;

            LocalDate ld = null;

            try
            {

                  ld = LocalDate.parse(potDate, DateTimeFormatter.ofPattern("M/d/yyyy"));
                  LOGGER.info(ld.toString());
                  if (ld.isBefore(LocalDate.now()))
                  {
                        return false;
                  }
                  // return ld.isAfter(classroom.getClassStartDate()) && ld.isBefore(classroom.getClassEndDate()); commented out because other things arent implemented yet
                  return true;
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

      public static List<Long> checkRoles(Member member)
      {
            return null;
      }

}
