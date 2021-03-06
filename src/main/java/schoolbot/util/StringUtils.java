package schoolbot.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class StringUtils
{
      private StringUtils()
      {
      }

      public static String formatDate(OffsetDateTime dateTime)
      {
            return dateTime.format(DateTimeFormatter.ofPattern("M/dd/yyyy **@** HH:mm:ss"));
      }

      public static String formatDate(LocalDate dateTime)
      {
            return dateTime.format(DateTimeFormatter.ofPattern("M/dd/yyyy"));
      }


      public static String formatDate(LocalDateTime time)
      {
            return time.format(DateTimeFormatter.ofPattern("M/dd/yyyy **@** HH:mm:ss"));
      }

      public static String parseNumberWithCommas(Object o)
      {
            return new DecimalFormat("#,###.00").format(o);
      }

      public static String hyperText(String text, String link)
      {
            return String.format("[%s](%s)", text, link);
      }

}
