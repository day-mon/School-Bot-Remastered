package schoolbot.natives.objects.command;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;

public class CommandEvent 
{
    private final GuildMessageReceivedEvent event;
    private final Schoolbot schoolbot;
    private final Command command;
    private final List<String> args;

    public CommandEvent(GuildMessageReceivedEvent event, Command command, List<String> args, Schoolbot schoolbot) 
    {
        this.event = event;
        this.command = command;
        this.args = args;
        this.schoolbot = schoolbot;
    }

    public GuildMessageReceivedEvent getEvent() 
    {
        return event;
    }

    public Schoolbot getSchoolbot() 
    {
        return schoolbot;
    }

    public Command getCommand()
    {
        return command;
    }

    public List<String> getArgs()
    {
        return args;
    }

    public Guild getGuild()
    {
        return event.getGuild();
    }

    public TextChannel getTextChannel()
    {
        return event.getChannel();
    }

    public User getUser()
    {
        return event.getAuthor();
    }

    public Member getMember()
    {
        return event.getMember();
    }

    public MessageChannel getChannel()
    {
        return event.getChannel();
    }

    public JDA getJDA()
    {
        return event.getJDA();
    }

    public Message getMessage()
    {
        return event.getMessage();
    }   

    public boolean memberPermissionCheck(List<Permission> list) 
    {
        return (getMember() != null && getMember().hasPermission(event.getChannel(), list));
    } 

    public boolean selfPermissionCheck(List<Permission> list)
    {
        return event.getGuild().getSelfMember().hasPermission(list);
    }

    public boolean selfPermissionCheck(Permission... permissions)
    {
        return event.getGuild().getSelfMember().hasPermission(permissions);
    }

    public void sendMessage(String message)
    {
        getChannel().sendMessage(message).queue();
    }

    public void sendMessage(EmbedBuilder embedBuilder)
    {
        getChannel().sendMessage(
            embedBuilder.setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR)
                        .setTimestamp(Instant.now()).build())
                        .queue();
    }


    public void sendMessage(EmbedBuilder embedBuilder, Color color)
    {
        getChannel().sendMessage(
            embedBuilder.setColor(color)
                        .setTimestamp(Instant.now()).build())
                        .queue();
    }

    public void sendSelfDeletingMessage(String message)
    {
        getChannel().sendMessage(message).queue(deleting -> {
            deleting.delete().queueAfter(10, TimeUnit.SECONDS);
        });
    }
    
}
