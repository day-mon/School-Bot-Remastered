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

      public static boolean classTime(Schoolbot schoolbot, String time, Classroom classroom)
      {

            if (time.isEmpty() || time.isBlank()) return false;
            if (!time.contains(":")) return false;
            if (!time.contains("am") && !time.contains("pm")) return false;
            // too lazy to do negation rn
            if (time.contains("Mo") || time.contains("Tu") || time.contains("We") || time.contains("Th") || time.contains("Fr"))
                  ;
            else return false;


            Map<DayOfWeek, LocalDateTime> stuff = parseTime(classroom, time);


            LocalDate ld = classroom.getStartDate().isBefore(LocalDate.now()) ? LocalDate.now() : classroom.getStartDate();

            DatabaseUtil.removeClassReminderByClass(schoolbot, classroom);

            // Could speed this up by only jumping to valid days
            // TODO: Switch to while loop and only jump to days we need.

            while (ld.isBefore(classroom.getEndDate()))
            {
                  if (ld.isAfter(LocalDate.now()))
                  {
                        DayOfWeek day = ld.getDayOfWeek();
                        if (stuff.containsKey(day))
                        {
                              LocalTime localTime = stuff.get(day).toLocalTime();

                              DatabaseUtil.addClassReminder(schoolbot, LocalDateTime.of(ld, localTime), List.of(60, 30, 10), classroom);

                        }
                  }
                  ld = ld.plusDays(1);
            }
            /*
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
             */
            return true;
      }

      public static boolean parseClassEditTime(String potTime, Classroom classroom)
      {
            Map<DayOfWeek, LocalDateTime> dayz = new HashMap<>();
            Map<String, DayOfWeek> stringDayOfWeekMap = Map.of(
                    "Mo", DayOfWeek.MONDAY,
                    "Tu", DayOfWeek.TUESDAY,
                    "We", DayOfWeek.WEDNESDAY,
                    "Th", DayOfWeek.THURSDAY,
                    "Fr", DayOfWeek.FRIDAY
            );

            String time = potTime.toLowerCase();

            if (time.isBlank() || time.isBlank()) return false;
            if (!time.contains(":")) return false;
            if (!time.contains("am") && !time.contains("pm")) return false;
            if (!potTime.contains("Mo") && !potTime.contains("Tu") && !potTime.contains("We") && !potTime.contains("Th") && !potTime.contains("Fr"))
                  return false;


            String[] initialSplit = potTime.split("\\s+");
            String days = initialSplit[0];
            String[] daysSplit = days.split("(?=\\p{Upper})");

            for (String day : daysSplit)
            {
                  if (stringDayOfWeekMap.containsKey(day))
                  {
                        //dayz.put()
                  }
            }
            return false;
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

            int hour;
            int minute;

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