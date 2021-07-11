package schoolbot.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Commands extends Command
{
      public Commands()
      {
            super("List all available commands", "[none]", 0);
            addCalls("commands", "cmds");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var cmds = event.getSchoolbot().getCommandHandler().getFilteredCommands();
            var prefix = event.getGuildPrefix();


            List<String> pages = new ArrayList<>();
            cmds.stream().collect(Collectors.groupingBy(Command::getCategory))
                    .forEach((commandCategory, commands) ->
                    {
                          if (commands.isEmpty()) return;
                          var page = new StringBuilder().append("**").append(commandCategory.getEmoji().getAsChat()).append(" ").append(commandCategory.getName()).append("**");


                          commands.forEach(cmd -> page.append("\n• `").append(prefix).append(cmd.getName().toLowerCase()).append("` - *").append(cmd.getDescription()).append("*"));

                          pages.add(page.toString());

                    });


            List<MessageEmbed> embeds = new ArrayList<>();

            var pageSize = pages.size();
            for (int i = 0; i < pageSize; i++)
            {
                  embeds.add(new EmbedBuilder()
                          .setDescription(pages.get(i))
                          .setColor(Constants.DEFAULT_EMBED_COLOR)
                          .setFooter(String.format("Page %d/%d", i+1, pageSize))
                          .setTimestamp(Instant.now())
                          .setAuthor("Commands", "https://schoolbot.dev", event.getJDA().getSelfUser().getAvatarUrl())
                          .build()
                  );
            }


            event.sendAsPaginator(embeds);
      }
}
