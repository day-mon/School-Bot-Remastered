package schoolbot.events;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;

public class MessageRecieve extends ListenerAdapter {

    private final Schoolbot schoolbot;

    public MessageRecieve(Schoolbot schoolbot)
    {
        this.schoolbot = schoolbot;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) 
    {
        String message = event.getMessage().getContentRaw();

        if (!message.startsWith(SchoolbotConstants.DEFAULT_PREFIX))
        {
            return;
        }

        schoolbot.getCommandHandler().handle(event);

    }
    
}