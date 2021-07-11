package schoolbot.listener;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;

public class ChannelListener extends ListenerAdapter
{
      private final Schoolbot schoolbot;
      private final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

      public ChannelListener(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }

      @Override
      public void onTextChannelDelete(@NotNull TextChannelDeleteEvent event)
      {
            var jda = event.getJDA();
            var selfUser = jda.getSelfUser();
            var guild = event.getGuild();
            var perms = event.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS);

            if (!perms)
            {
                  LOGGER.error("Self user does not have permissions to view audit logs to attempt to alert user about channel deletion if it occured");
                  return;
            }

            guild.retrieveAuditLogs()
                    .type(ActionType.CHANNEL_DELETE)
                    .limit(1)
                    .queue(channelDelete ->
                    {
                          var channelDeleteUser = channelDelete.get(0).getUser();


                          if (selfUser.getIdLong() == channelDeleteUser.getIdLong())
                          {
                                return;
                          }

                          schoolbot.getWrapperHandler().getClasses(guild.getIdLong())
                                  .stream()
                                  .filter(channel -> channel.getChannelID() == event.getChannel().getIdLong())
                                  .findFirst()
                                  .ifPresent(classroom ->
                                  {
                                        var defaultChannel = event.getGuild().getDefaultChannel() == null ? event.getGuild().getSystemChannel() : event.getGuild().getDefaultChannel();
                                        var user = jda.getUserById(event.getGuild().getOwnerId());
                                        var channel = event.getChannel();

                                        if (defaultChannel != null)
                                        {
                                              defaultChannel.sendMessageFormat
                                                      ("** %s ** has deleted.. It belonged to ** %s **. I can no longer alert you based off your role you can edit the class and set the role by using class edit.", channel.getName(), classroom.getName()).queue();
                                              LOGGER.info("{} has been warned in the default channel", guild.getName());
                                              return;
                                        }

                                        LOGGER.warn("{} has no default or system channel!", event.getGuild().getName());

                                        if (user != null)
                                        {
                                              user.openPrivateChannel()
                                                      .flatMap(privateMessage -> privateMessage.sendMessageFormat
                                                              ("** %s ** has deleted.. It belonged to ** %s **. I can no longer alert you based off your role you can edit the class and set the role by using class edit.", channel.getName(), classroom.getName())).queue();
                                              return;
                                        }

                                        LOGGER.warn("{} is owner-less.. Nothing I can do to alert", event.getGuild().getName());
                                  });

                    }, faulure -> LOGGER.info("Could not retrieve audit logs."));
      }
}
