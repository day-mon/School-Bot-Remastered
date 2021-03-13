package schoolbot.commands.misc;

import java.time.Duration;
import java.time.LocalDateTime;

import net.dv8tion.jda.api.EmbedBuilder;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class Uptime extends Command 
{
    public Uptime()
    {
        super("Displays uptime for bot", "[none]", 0);
        addCalls("uptime", "utime");
    }
    
    @Override
    public void run(CommandEvent event) 
    {

        Duration timeBetween = Duration.between(event.getSchoolbot().getBotStartTime(), LocalDateTime.now());
        event.sendMessage(new EmbedBuilder()
                            .setDescription(
                                "Uptime: " + timeBetween.toDaysPart() +
								" days, " + timeBetween.toHoursPart() +
								" hours, " + timeBetween.toMinutesPart() +
                                " minutes, " + timeBetween.toSecondsPart() +
								" seconds.")
				.setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR));
    }
}
