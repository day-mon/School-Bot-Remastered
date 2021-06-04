package schoolbot.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Uptime extends Command
{
      public Uptime()
      {
            super("Displays uptime for bot", "[none]", 0);
            addCalls("uptime", "utime");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

            Duration timeBetween = Duration.between(event.getSchoolbot().getBotStartTime(), LocalDateTime.now());
            event.sendMessage(new EmbedBuilder()
                    .setDescription(
                            "Uptime: " + timeBetween.toDaysPart() +
                            " days, " + timeBetween.toHoursPart() +
                            " hours, " + timeBetween.toMinutesPart() +
                            " minutes, " + timeBetween.toSecondsPart() +
                            " seconds.")
                    .setColor(Constants.DEFAULT_EMBED_COLOR));
      }
}
