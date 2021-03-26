package schoolbot.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;

import javax.annotation.Nonnull;

public class MainListener implements EventListener
{
    private final Schoolbot schoolbot;

    public MainListener(Schoolbot schoolbot)
    {
        this.schoolbot = schoolbot;
    }


    public void onEvent(@Nonnull GenericEvent event)
    {
        if (event instanceof GuildMessageReceivedEvent)
        {
            String message = ((GuildMessageReceivedEvent) event).getMessage().getContentRaw();
            User author = ((GuildMessageReceivedEvent) event).getAuthor();

            schoolbot.getLogger().info(author.getAsTag() + " has sent: " + message);


            if (!message.startsWith(SchoolbotConstants.DEFAULT_PREFIX))
            {
                return;
            }

            schoolbot.getCommandHandler().handle((GuildMessageReceivedEvent) event);

        }

        else if (event instanceof GuildMemberJoinEvent)
        {

        }
    }
}
