package schoolbot.commands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class StateMachineTest extends ListenerAdapter
{
    private final long channelID, authorID;

    private int state = 0;

    public StateMachineTest(long channelID, long authorID)
    {
        this.channelID = channelID;
        this.authorID = authorID;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event)
    {
        if (event.getAuthor().getIdLong() != authorID)
        {
            return;
        }
        if (event.getChannel().getIdLong() != channelID)
        {
            return;
        }

        MessageChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();

        switch (state)
        {
            case 0:
                channel.sendMessage("Hello " + event.getMember().getNickname() + " You would like to add a school? What is this name?: ").queue();
                state = 1;
                break;
            case 1:
                channel.sendMessage("Your school name is " + message).queue();
                break;

        }


    }
}
