package schoolbot.listener;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import schoolbot.Schoolbot;

import javax.annotation.Nonnull;

public class MainListener implements EventListener
{
      private final Schoolbot schoolbot;

      public MainListener(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }

      @Override
      public void onEvent(@Nonnull GenericEvent event)
      {
            if (event instanceof GuildMessageReceivedEvent)
            {
                  GuildMessageReceivedEvent eve = (GuildMessageReceivedEvent) event;

                  String message = eve.getMessage().getContentRaw();
                  User author = eve.getAuthor();

                  schoolbot.getLogger().info("[{}] -> {} ({}) has sent the message: {} ", eve.getGuild().getName(), author.getAsTag(), author.getId(), message);


                  schoolbot.getMessageHandler().handle((GuildMessageReceivedEvent) event);

            }
      }
}
