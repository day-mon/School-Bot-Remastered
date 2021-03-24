package schoolbot.commands.admin;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.util.Embed;

public class Prune extends Command
{

    public Prune()
    {
        super("Deletes all messages associted with the bot in the last 100 messages.", "[none]", 0);
        addCalls("prune");
        addPermissions(Permission.MESSAGE_MANAGE);
        addSelfPermissions(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void run(CommandEvent event) 
    {
        MessageChannel channel = event.getChannel();
        event.getTextChannel().getIterableHistory()
                .takeAsync(100)
                .thenApplyAsync(channelMessages -> {
                    List<Message> deleteAbleMessages =
                            channelMessages.stream()
                                    .filter(message -> message.getId().equals(SchoolbotConstants.GENIUS_ID_STRING))
                                    .collect(Collectors.toList());
                    channel.purgeMessages(deleteAbleMessages);
                    return deleteAbleMessages.size();
                }).whenCompleteAsync((messagetotal, throwable) -> channel.sendMessage(Emoji.RECYCLE.getAsChat() + "Cleared `" + messagetotal + "` messages!" )
                        .queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));




    }
    
}
