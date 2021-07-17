package schoolbot.commands.admin;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;
import schoolbot.util.EmbedUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Prune extends Command
{

      public Prune()
      {
            super("Deletes all messages associated with the bot in the last 100 messages.", "[none]", 0);
            addCalls("prune", "recycle");
            addPermissions(Permission.MESSAGE_MANAGE);
            addSelfPermissions(Permission.MESSAGE_MANAGE);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var selfUser = event.getJDA().getSelfUser();
            var prefix = event.getGuildPrefix();

            event.getTextChannel().getIterableHistory()
                    .takeAsync(100)
                    .thenApplyAsync(messages ->
                    {
                          List<Message> messagesToDelete = messages
                                  .stream()
                                  .filter(msg -> msg.getContentRaw().startsWith(prefix) || msg.getAuthor().getIdLong() == selfUser.getIdLong())
                                  .filter(msg -> Duration.between(msg.getTimeCreated(), event.getMessage().getTimeCreated()).toHours() <= 24)
                                  .collect(Collectors.toList());

                          event.getChannel().purgeMessages(messagesToDelete);

                          return messagesToDelete.size();
                    })
                    .whenCompleteAsync((messageTotal, throwable) ->
                    {
                          if (throwable != null)
                          {
                                EmbedUtils.error(event, "Error has occurred whilst trying to delete messages");
                                return;
                          }

                          if (messageTotal == 0)
                          {
                                EmbedUtils.error(event, "There are no valid messages to prune");
                                return;
                          }

                          event.getChannel().sendMessage(
                                  Emoji.RECYCLE.getAsChat() + " Successfully purged `" + messageTotal + "` messages"
                          ).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS, null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER)));
                    });

      }
}
