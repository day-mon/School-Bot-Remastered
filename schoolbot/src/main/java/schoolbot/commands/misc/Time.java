package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Embed;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Time extends Command
{
    public Time()
    {
        super("Displays time of any timezone", "[optional: timezone/random]", 0);
        addCalls("time", "localtime");
    }

    @Override
    public void run(CommandEvent event)
    {
        List<String> timeZones = Arrays.asList(TimeZone.getAvailableIDs());


        if (event.getArgs().size() == 0)
        {
            event.sendMessage(new EmbedBuilder()
                        .setDescription(new SimpleDateFormat().format(new Date()))
                        .setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR));
        }
        else 
        {
            if (event.getArgs().get(0).equalsIgnoreCase("random")) 
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
                String timeZoneCheck = event.getArgs().size() <= 3 ? event.getArgs().get(0).toUpperCase() : event.getArgs().get(0);
                if (!timeZones.contains(timeZoneCheck)) //This is good practice
                {
                    Embed.error(event, "That is not a valid timezone!");
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
