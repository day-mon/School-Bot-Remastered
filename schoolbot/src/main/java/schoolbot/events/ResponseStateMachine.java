package schoolbot.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Embed;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;

public class ResponseStateMachine extends ListenerAdapter
{
    private final long userID;
    private final long channelID;
    private final Command com;

    private int state = 0;

    public ResponseStateMachine(User user, MessageChannel channel, Command com)
    {
        this.userID = user.getIdLong();
        this.channelID = channel.getIdLong();
        this.com = com;
    }


    public void onCommandEvent(@Nonnull CommandEvent commandEvent)
    {
        if (commandEvent.getCommand() != com) return;
        if (commandEvent.getUser().getIdLong() != userID) return;
        if (commandEvent.getChannel().getIdLong() != channelID) return;

        MessageChannel channel = commandEvent.getChannel();
        Message message = commandEvent.getMessage();
        User user = commandEvent.getUser();

        String schoolname = "";
        String schoolEmail = "";

    }



}
