package schoolbot.commands.admin;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
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
        List<Message> filteredMessages = event.getTextChannel().getIterableHistory().stream()
                                                                        .filter(msg -> msg.getAuthor().getId().equals(SchoolbotConstants.GENIUS_ID_STRING) || msg.getContentRaw().startsWith("-"))
                                                                        .filter(msg -> Duration.between(msg.getTimeCreated(), event.getMessage().getTimeCreated()).toHours() <= 24)
                                                                        .limit(100)
                                                                        .collect(Collectors.toList());

        
        event.getChannel().purgeMessages(filteredMessages);

        event.sendSelfDeletingMessage(Emoji.RECYCLE.getAsChat() + " Cleared `" + filteredMessages.size() + "` messages!");
    }
    
}
