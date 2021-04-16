package schoolbot.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import schoolbot.Schoolbot;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

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

            schoolbot.getMessageHandler().handle((GuildMessageReceivedEvent) event);
        }

        else if (event instanceof GuildMemberJoinEvent)
        {
            TextChannel channel = ((GuildMemberJoinEvent) event).getGuild().getDefaultChannel();
            Guild guild = ((GuildMemberJoinEvent) event).getGuild();
            String guildName = ((GuildMemberJoinEvent) event).getGuild().getName();
            User user = ((GuildMemberJoinEvent) event).getUser();


            channel.sendMessage(new EmbedBuilder()
                    .setTitle("Welcome to " + guildName, "http://pittmainrejects.net")
                    .setFooter("Joined on: " + LocalDateTime.now())
                    .addField("User information",
                            "`User joined:`" + user.getName() + "\n" + "`Account creation date:` " + user.getTimeCreated(), false)
                    .addField("Sever Information",
                            "`Server count:`  " + guild.getMemberCount() + "\n" + "`Server name:`  " + guild.getName(), false)
                    .addField("Description",
                            "Welcome " + user.getName() + " if you are here you probably have no clue what you are doing like all " + guild.getMemberCount() + " of us. If you need some help you can mention anyone indivisually or mention the role of the class in which you need help in. Mention anyone in 8 and above for roles.", false)
                    .build()).queue();

        }
    }
}
