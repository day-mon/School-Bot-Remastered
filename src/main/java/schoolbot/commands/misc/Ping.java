package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;

import java.util.List;

public class Ping extends Command
{
      public Ping()
      {
            super("", "", 0);
            addCalls("ping");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            JDA jda = event.getJDA();
            Member member = event.getMember();

            jda.getRestPing()
                    .queue(restPing ->
                            event.sendMessage(
                                    new EmbedBuilder()
                                            .addField("Gateway Ping", Emoji.HOURGLASS.getAsChat() + " " + jda.getGatewayPing() + "ms", false)
                                            .addField("Rest Ping", Emoji.STOPWATCH.getAsChat() + " " + restPing + "ms", false)
                                            .setFooter("Ping tested by " + member.getNickname(), member.getUser().getAvatarUrl())
                                            .build()
                            ));
      }
}
