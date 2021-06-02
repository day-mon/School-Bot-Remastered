package schoolbot.util;

import schoolbot.Schoolbot;
import schoolbot.objects.school.Classroom;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Parser
{
      public static List<String> args(String stringArgs)
      {
            List<String> args = new ArrayList<>();

        /*
          Split args by spaces
         */
            String[] splitArgs = stringArgs.split("\\s+");
            int quoteCount = 0;
            StringBuilder tempString = new StringBuilder();

            for (int i = 0; i < splitArgs.length; i++)
            {
                  if (splitArgs[i].contains("\"") || splitArgs[i].contains("”"))
                  {
                        quoteCount++;
                        int temp = i;

                        while (temp != splitArgs.length && quoteCount != 2)
                        {
                              tempString.append(splitArgs[i]).append(" ");
                              if (i + 1 != splitArgs.length) i++;
                              else break;
                              if (i != temp && splitArgs[i].contains("\"") || splitArgs[i].contains("”"))
                              {
                                    quoteCount++;
                                    tempString.append(splitArgs[i]);
                              }
                        }
                        tempString = new StringBuilder(tempString.toString().trim().replaceAll("\"", ""));
                        args.add(tempString.toString());
                        quoteCount = 0;
                        tempString = new StringBuilder();
                  }
                  else
                  {
                        args.add(splitArgs[i]);
                  }
            }
            return args;
      }

      public static void classTime(Schoolbot schoolbot, String time, Classroom classroom)
      {

            if (time.isEmpty() || time.isBlank())
            {
                  return;
            }


            Map<DayOfWeek, LocalDateTime> stuff = parseTime(classroom, time);


            LocalDate ld = classroom.getStartDate().isBefore(LocalDate.now()) ? LocalDate.now() : classroom.getStartDate();

            // Could speed this up by only jumping to valid days
            for (; ld.isBefore(classroom.getEndDate()); ld = ld.plusDays(1))
            {
                  if (ld.isAfter(LocalDate.now()))
                  {
                        if (stuff.containsKey(ld.getDayOfWeek()))
                        {
                              LocalTime s = stuff.get(ld.getDayOfWeek()).toLocalTime();

                              DatabaseUtil.addClassReminder(schoolbot, LocalDateTime.of(ld, s), List.of(60, 30, 10), classroom);
                        }
                  }
            }
      }

      private static Map<DayOfWeek, LocalDateTime> parseTime(Classroom classroom, String time)
      {

            Map<DayOfWeek, LocalDateTime> s = new HashMap<>();

            /*
              Mo = Monday
              We = Wednesday
              Fr = Friday
              Tu = Tuesday
              Th = Thursday
             */
            Map<String, DayOfWeek> stringDayOfWeekMap = Map.of(
                    "Mo", DayOfWeek.MONDAY,
                    "Tu", DayOfWeek.TUESDAY,
                    "We", DayOfWeek.WEDNESDAY,
                    "Th", DayOfWeek.THURSDAY,
                    "Fr", DayOfWeek.FRIDAY
            );


            String[] initialSplit = time.split("\\s+");
            String days = initialSplit[0];
            String classTime = initialSplit[1].toLowerCase();
            String[] daysSplit = days.split("(?=\\p{Upper})");

            int hour = 0;
            int minute = 0;

            String[] classTimeSplit = classTime.split(":");

            if (classTime.toLowerCase().contains("am"))
            {
                  hour = Integer.parseInt(classTimeSplit[0]);
                  minute = Integer.parseInt(classTimeSplit[1].replaceAll("am", ""));
            }
            else
            {
                  hour = Integer.parseInt(classTimeSplit[0]);
                  minute = Integer.parseInt(classTimeSplit[1].replaceAll("pm", ""));
            }

            for (String sd : daysSplit)
            {
                  if (stringDayOfWeekMap.containsKey(sd))
                  {
                        s.put(stringDayOfWeekMap.get(sd), LocalDateTime.of(classroom.getStartDate(), LocalTime.of(hour, minute)));
                  }
            }
            return s;
      }
}