package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Time extends Command
{
      public Time()
      {
            super("Displays time of any timezone", "<timezone/random>", 0);
            addCalls("time", "localtime");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            List<String> timeZones = Arrays.asList(TimeZone.getAvailableIDs());


            if (args.size() == 0)
            {
                  event.sendMessage(new EmbedBuilder()
                          .setDescription(new SimpleDateFormat().format(new Date()))
                          .setColor(Constants.DEFAULT_EMBED_COLOR));
            }
            else
            {
                  if (args.get(0).equalsIgnoreCase("random"))
                  {
                        int randomTimeZoneIndex = new Random().nextInt(timeZones.size());
                        String timeZone = timeZones.get(randomTimeZoneIndex);

                        DateFormat df = new SimpleDateFormat(); //This is outdated

                        df.setTimeZone(TimeZone.getTimeZone(timeZone));
                        event.sendMessage(new EmbedBuilder()
                                .setDescription("TimeZone name: " + df.getTimeZone().getDisplayName()
                                                + "\nTimeZone time: " + df.format(new Date())
                                                + "\nTimeZone ID: " + df.getTimeZone().getID()));
                  }
                  else
                  {
                        String timeZoneCheck = args.size() <= 3 ? args.get(0).toUpperCase() : args.get(0);
                        if (!timeZones.contains(timeZoneCheck)) //This is good practice
                        {
                              EmbedUtils.error(event, "That is not a valid timezone!");
                              return;
                        }

                        DateFormat df = new SimpleDateFormat(); //This is outdated

                        df.setTimeZone(TimeZone.getTimeZone(timeZoneCheck));
                        event.sendMessage(new EmbedBuilder()
                                .setDescription("TimeZone name: " + df.getTimeZone().getDisplayName()
                                                + "\nTimeZone time: " + df.format(new Date())
                                                + "\nTimeZone ID: " + df.getTimeZone().getID()));
                  }
            }

      }
}
