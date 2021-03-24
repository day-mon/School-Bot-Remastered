package schoolbot.events;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class UserServerJoin extends ListenerAdapter
{
    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event)
    {
        Guild guild = event.getGuild();
        User user = event.getUser();

    }
}
