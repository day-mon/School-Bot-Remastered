package schoolbot.listeners;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;

public class MessageListener extends ListenerAdapter
{
      private final Schoolbot schoolbot;
      private final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

      public MessageListener(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }


      @Override
      public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
      {
            String message = event.getMessage().getContentRaw();
            User author = event.getAuthor();

            LOGGER.info("[{}] -> {} ({}) has sent the message: {} ", event.getGuild().getName(), author.getAsTag(), author.getId(), message);


            schoolbot.getMessageHandler().handle(event);
      }
}
