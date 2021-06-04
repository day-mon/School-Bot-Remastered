package schoolbot.listener;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;

import javax.annotation.Nonnull;

public class MainListener implements EventListener
{
      private final Schoolbot schoolbot;
      private final Logger LOGGER = LoggerFactory.getLogger(MainListener.class);

      public MainListener(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }

      @Override
      public void onEvent(@Nonnull GenericEvent genericEvent)
      {
            if (genericEvent instanceof GuildMessageReceivedEvent event)
            {

                  String message = event.getMessage().getContentRaw();
                  User author = event.getAuthor();

                  schoolbot.getLogger().info("[{}] -> {} ({}) has sent the message: {} ", event.getGuild().getName(), author.getAsTag(), author.getId(), message);


                  schoolbot.getMessageHandler().handle(event);
            }
            else if (genericEvent instanceof RoleDeleteEvent event)
            {
                  var channel = event.getGuild().getDefaultChannel();
                  var jda = event.getJDA();
                  var selfUser = jda.getSelfUser();

                  System.out.println("happened");
                  event.getGuild().retrieveAuditLogs()
                          .type(ActionType.ROLE_DELETE)
                          .limit(1)
                          .queue(list ->
                          {
                                User userWhoRemovedRole = list.get(0).getUser();

                                if (userWhoRemovedRole.getIdLong() == selfUser.getIdLong())
                                {
                                      return;
                                }

                                schoolbot.getWrapperHandler().getClasses(event.getGuild().getIdLong())
                                        .stream()
                                        .filter(classRole -> classRole.getRoleID() == event.getRole().getIdLong())
                                        .findFirst()
                                        .ifPresent(classroom ->
                                        {
                                              TextChannel defaultChannel = event.getGuild().getDefaultChannel() == null ? event.getGuild().getSystemChannel() : event.getGuild().getDefaultChannel();
                                              User user = jda.getUserById(event.getGuild().getOwnerId());

                                              System.out.println("user = " + user);

                                              if (defaultChannel == null)
                                              {
                                                    LOGGER.warn("{} has no default or system channel!", event.getGuild().getName());
                                                    if (user != null)
                                                    {
                                                          user.openPrivateChannel()
                                                                  .flatMap(privateChannel -> privateChannel.sendMessageFormat
                                                                          ("** %s ** has deleted.. It belonged to ** %s **. I can no longer alert you based off your role you can edit the class and set the role by using class edit."))
                                                                  .queue();
                                                    }
                                                    else
                                                    {
                                                          LOGGER.warn("{} is owner-less.. Nothing I can do to alert", event.getGuild().getName());
                                                    }
                                              }
                                              else
                                              {
                                                    System.out.println("what happen");
                                                    defaultChannel.sendMessageFormat
                                                            ("** %s ** has deleted.. It belonged to ** %s **. I can no longer alert you based off your role you can edit the class and set the role by using class edit.")
                                                            .queue();
                                              }
                                        });
                          });
            }
            else if (genericEvent instanceof TextChannelDeleteEvent event)
            {

            }
      }
}
