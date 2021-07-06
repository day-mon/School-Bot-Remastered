package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.util.List;

public class PrefixView extends Command
{
      public PrefixView(Command parent)
      {
            super(parent, "Gets current prefix of the server and default schoolbot prefix", "[none]", 0);
            addUsageExample("prefix view");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var guild = event.getGuild();
            var user = event.getUser();
            var prefix = event.getGuildPrefix().equals(Constants.DEFAULT_PREFIX) ? "No Custom Prefix Set" : event.getGuildPrefix();

            event.sendMessage(new EmbedBuilder()
                    .setTitle("Guild Prefix", "https://schoolbot.dev")
                    .addField("Guild Prefix", prefix, false)
                    .addField("Default Schoolbot Prefix", Constants.DEFAULT_PREFIX, false)
                    .setFooter("Command Executed by: " + user.getAsTag(), user.getAvatarUrl())
            );
      }
}
