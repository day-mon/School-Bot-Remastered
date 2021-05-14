package schoolbot.natives.util;

import java.util.ArrayList;
import java.util.List;


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
                  if (splitArgs[i].contains("\""))
                  {
                        quoteCount++;
                        int temp = i;

                        while (temp != splitArgs.length && quoteCount != 2)
                        {
                              tempString.append(splitArgs[i]).append(" ");
                              if (i + 1 != splitArgs.length) i++;
                              else break;
                              if (i != temp && splitArgs[i].contains("\""))
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
}
