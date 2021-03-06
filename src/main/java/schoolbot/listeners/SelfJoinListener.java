package schoolbot.listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.util.EmbedUtils;

public class SelfJoinListener extends ListenerAdapter
{
      private final Logger LOGGER = LoggerFactory.getLogger(SelfJoinListener.class);
      private final Schoolbot schoolbot;

      public SelfJoinListener(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }

      @Override
      public void onGuildJoin(@NotNull GuildJoinEvent event)
      {
            var guild = event.getGuild();
            var channel = guild.getDefaultChannel() == null ?
                    guild.getSystemChannel() : guild.getDefaultChannel();


            if (channel == null)
            {
                  guild.retrieveOwner().queue(owner ->
                  {
                        var ownerUser = owner.getUser();

                        ownerUser.openPrivateChannel().queue(privateChannel -> EmbedUtils.sendTutorial(privateChannel, schoolbot, guild));
                  }, failure -> LOGGER.error("Error has occurred whilst retrieving owner", failure));
                  return;
            }

            EmbedUtils.sendTutorial(channel, schoolbot, guild);
      }
}
