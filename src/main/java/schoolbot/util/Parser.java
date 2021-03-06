package schoolbot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.school.Classroom;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


public class Parser
{

      private static final Logger logger = LoggerFactory.getLogger(Parser.class);

      private Parser() {}

      public static List<String> args(String stringArgs)
      {
            List<String> args = new ArrayList<>();

        /*
          Split args by spaces
         */
            String[] splitArgs = stringArgs.split("\\s+");
            int quoteCount = 0;
            StringBuilder tempString = new StringBuilder();

            var length = splitArgs.length;

            for (int i = 0; i < length; i++)
            {
                  if (splitArgs[i].contains("'") || splitArgs[i].contains("’"))
                  {
                        quoteCount++;
                        int temp = i;

                        while (temp != length && quoteCount != 2)
                        {
                              tempString.append(splitArgs[i]).append(" ");
                              if (i + 1 != splitArgs.length) i++;
                              else break;
                              if (i != temp && splitArgs[i].contains("'") || splitArgs[i].contains("’"))
                              {
                                    quoteCount++;
                                    tempString.append(splitArgs[i]);
                              }
                        }
                        tempString = new StringBuilder(tempString.toString().trim().replaceAll("'", "").replaceAll("’", ""));
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

            String timeLoweCased = time.toLowerCase();

            if (timeLoweCased.isEmpty() || time.isBlank()) return false;
            if (!timeLoweCased.contains(":")) return false;
            if (!timeLoweCased.contains("pm") && !timeLoweCased.contains("am")) return false;
            if (!timeLoweCased.contains("mo") && !timeLoweCased.contains("tu") && !timeLoweCased.contains("we") && !timeLoweCased.contains("th") && !timeLoweCased.contains("fr"))
                  return false;

            DatabaseUtils.removeClassReminderByClass(schoolbot, classroom);

            Map<DayOfWeek, LocalDateTime> localDateTimeMap = parseTime(classroom, time);

            if (Objects.isNull(localDateTimeMap))
            {
                  logger.error("Parse Time method has produced a null pointer exception");
                  return false;
            }

            List<DayOfWeek> dayOfWeekList = new ArrayList<>(localDateTimeMap.keySet())
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());


            LocalDate ld = classroom.getStartDate().isBefore(LocalDate.now()) ? LocalDate.now() : classroom.getStartDate();

            while (ld.isBefore(classroom.getEndDate()) || ld.isEqual(classroom.getEndDate()))
            {
                  DayOfWeek day = ld.getDayOfWeek();
                  if (localDateTimeMap.containsKey(day))
                  {
                        LocalTime localTime = localDateTimeMap.get(day).toLocalTime();
                        classroom.setTime(localTime);

                        DatabaseUtils.addClassReminder(schoolbot, LocalDateTime.of(ld, localTime), List.of(60, 30, 10, 0), classroom);

                        if (dayOfWeekList.get(dayOfWeekList.size() - 1) == ld.getDayOfWeek())
                        {
                              DayOfWeek beginning = dayOfWeekList.get(0);
                              ld = ld.with(TemporalAdjusters.next(beginning));
                        }
                        else
                        {
                              DayOfWeek nextDay = dayOfWeekList
                                      .stream()
                                      .filter(dayOfWeek -> dayOfWeek.getValue() > day.getValue())
                                      .filter(localDateTimeMap::containsKey)
                                      .findFirst()
                                      .orElseThrow(() -> new IllegalStateException("I dont know how this happened"));
                              ld = ld.with(TemporalAdjusters.next(nextDay));
                        }
                        continue;
                  }
                  ld = ld.plusDays(1);
            }
            return true;
      }

      public static Map<DayOfWeek, LocalDateTime> parseTime(Classroom classroom, String time)
      {
            if (!timePrerequisites(time))
            {
                  return null;
            }

            Map<DayOfWeek, LocalDateTime> localDateTimeMap = new EnumMap<>(DayOfWeek.class);


            Map<String, DayOfWeek> stringDayOfWeekMap = Map.of(
                    "Mo", DayOfWeek.MONDAY,
                    "Tu", DayOfWeek.TUESDAY,
                    "We", DayOfWeek.WEDNESDAY,
                    "Th", DayOfWeek.THURSDAY,
                    "Thr", DayOfWeek.THURSDAY,
                    "Fr", DayOfWeek.FRIDAY,
                    "Fri", DayOfWeek.FRIDAY
            );


            String[] initialSplit = time.split("\\s+");
            String days = initialSplit[0];
            String classTime = initialSplit[1].toLowerCase();
            String[] daysSplit = days.split("(?=\\p{Upper})");

            if (daysSplit.length == 0) return null;

            int hour = 0;
            int minute = 0;

            String[] classTimeSplit = classTime.split(":");

            try
            {
                  if (classTime.toLowerCase().contains("am"))
                  {
                        hour = Integer.parseInt(classTimeSplit[0]);
                        minute = Integer.parseInt(classTimeSplit[1].toLowerCase().replaceAll("am", ""));
                  }
                  else
                  {
                        hour = Integer.parseInt(classTimeSplit[0]) + 12;

                        if (hour == 24)
                        {
                              hour = 12;
                        }

                        minute = Integer.parseInt(classTimeSplit[1].toLowerCase().replaceAll("pm", ""));
                  }
            }
            catch (Exception e)
            {
                  logger.error("Error whilst parsing {} or {}", hour, minute, e);
                  return null;
            }

            for (String day : daysSplit)
            {
                  if (stringDayOfWeekMap.containsKey(day))
                  {
                        localDateTimeMap.put(stringDayOfWeekMap.get(day), LocalDateTime.of(classroom.getStartDate(), LocalTime.of(hour, minute)));
                  }
            }
            return localDateTimeMap;
      }

      public static LocalDateTime parseTimeString(Date time, String strTime)
      {
            var localDate = Instant.ofEpochMilli(time.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            String[] initialSplit = strTime.split("\\s+");
            String days = initialSplit[0];
            String classTime = initialSplit[1].toLowerCase();
            String[] daysSplit = days.split("(?=\\p{Upper})");

            if (daysSplit.length == 0) return null;

            int hour = 0;
            int minute = 0;

            String[] classTimeSplit = classTime.split(":");

            try
            {
                  if (classTime.toLowerCase().contains("am"))
                  {
                        hour = Integer.parseInt(classTimeSplit[0]);
                        minute = Integer.parseInt(classTimeSplit[1].replaceAll("am", ""));
                  }
                  else
                  {
                        hour = Integer.parseInt(classTimeSplit[0]) + 12;

                        if (hour == 24)
                        {
                              hour = 12;
                        }
                        minute = Integer.parseInt(classTimeSplit[1].replaceAll("pm", ""));
                  }
            }
            catch (Exception e)
            {
                  logger.error("Error whilst parsing {} or {}", hour, minute, e);
                  return null;
            }
            return LocalDateTime.of(localDate, LocalTime.of(hour, minute));
      }


      private static boolean timePrerequisites(String time)
      {
            String timeLowerCased = time.toLowerCase();
            if (timeLowerCased.isEmpty() || time.isBlank()) return false;
            if (!timeLowerCased.contains(":")) return false;
            if (!timeLowerCased.contains("pm") && !timeLowerCased.contains("am")) return false;
            return timeLowerCased.contains("mo") || timeLowerCased.contains("tu") || timeLowerCased.contains("we") || timeLowerCased.contains("th") || timeLowerCased.contains("fr");
      }
}