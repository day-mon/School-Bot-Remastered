package schoolbot.listeners;

import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.util.DatabaseUtils;

public class SelfLeaveListener extends ListenerAdapter
{

      private static final Logger LOGGER = LoggerFactory.getLogger(SelfLeaveListener.class);
      private final Schoolbot schoolbot;

      public SelfLeaveListener(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }

      @Override
      public void onGuildLeave(@NotNull GuildLeaveEvent event)
      {
            var guild = event.getGuild();

            var successful = DatabaseUtils.removeAllGuildOccurrences(schoolbot, guild.getIdLong());

            if (!successful)
            {
                  LOGGER.error("Error has occurred whilst removing guild data for {} ({})", guild.getName(), guild.getIdLong());
            }
      }

}