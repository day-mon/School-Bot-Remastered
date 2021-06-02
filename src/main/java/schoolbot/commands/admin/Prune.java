package schoolbot.commands.admin;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import schoolbot.SchoolbotConstants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;

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
            event.getTextChannel().getIterableHistory()
                    .takeAsync(100)
                    .thenApplyAsync(messages ->
                    {
                          List<Message> messagesToDelete = messages
                                  .stream()
                                  .filter(msg -> msg.getContentRaw().startsWith(SchoolbotConstants.DEFAULT_PREFIX) || msg.getAuthor().getId().equals(SchoolbotConstants.GENIUS_ID_STRING))
                                  .filter(msg -> Duration.between(msg.getTimeCreated(), event.getMessage().getTimeCreated()).toHours() <= 24)
                                  .collect(Collectors.toList());

                          event.getChannel().purgeMessages(messagesToDelete);

                          return messagesToDelete.size();
                    })
                    .whenCompleteAsync((messageTotal, throwable) -> event.getChannel().sendMessage(
                            Emoji.RECYCLE.getAsChat() + " Successfully purged `" + messageTotal + "` messages"
                    ).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));

      }
}
