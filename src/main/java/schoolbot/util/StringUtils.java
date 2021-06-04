package schoolbot.util;

import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class StringUtils
{
      public StringUtils()
      {

      }

      public static String parseJoinDate(OffsetDateTime dateTime)
      {
            return dateTime.format(DateTimeFormatter.ofPattern("M/dd/yyyy **@** HH:mm:ss"));
      }

      public static String parseNumberWithCommas(Object o)
      {
            return new DecimalFormat("#,###.00").format(o);
      }
}
