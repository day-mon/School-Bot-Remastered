package schoolbot.listener;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.util.EmbedUtils;

public class SelfJoinListener extends ListenerAdapter
{
      private static final Logger LOGGER = LoggerFactory.getLogger(SelfJoinListener.class);

      @Override
      public void onGuildJoin(@NotNull GuildJoinEvent event)
      {
            var guild = event.getGuild();
            var channel = guild.getSystemChannel() == null ?
                    guild.getDefaultChannel() : guild.getSystemChannel();


            if (channel == null)
            {

                  guild.retrieveOwner().queue(owner ->
                  {
                        var ownerUser = owner.getUser();

                        ownerUser.openPrivateChannel().queue(EmbedUtils::sendTutorial);
                  }, failure -> LOGGER.error("Error has occurred whilst retrieving owner", failure));
                  return;
            }

            EmbedUtils.sendTutorial(channel);
      }
}
