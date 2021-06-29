package schoolbot.listener;

import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;


public class RoleListener extends ListenerAdapter
{
      private final Schoolbot schoolbot;
      private final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

      public RoleListener(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }

      @Override
      public void onRoleDelete(@NotNull RoleDeleteEvent event)
      {
            var jda = event.getJDA();
            var selfUser = jda.getSelfUser();
            var guild = event.getGuild();

            guild.retrieveAuditLogs()
                    .type(ActionType.ROLE_DELETE)
                    .limit(1)
                    .queue(roleDelete ->
                    {
                          var roleDeleteUser = roleDelete.get(0).getUser();
                          var role = event.getRole();

                          // If the role is deleted by the bot theres no need to check because it was during the clean up process
                          if (roleDeleteUser.getIdLong() == selfUser.getIdLong())
                          {
                                return;
                          }

                          schoolbot.getWrapperHandler().getClasses(guild.getIdLong())
                                  .stream()
                                  .filter(roleRemoved -> roleRemoved.getRoleID() == event.getRole().getIdLong())
                                  .findFirst()
                                  .ifPresent(classroom ->
                                  {
                                        var defaultChannel = event.getGuild().getDefaultChannel() == null ? event.getGuild().getSystemChannel() : event.getGuild().getDefaultChannel();
                                        var user = jda.getUserById(event.getGuild().getOwnerId());

                                        if (defaultChannel != null)
                                        {
                                              defaultChannel.sendMessageFormat
                                                      ("** %s ** has deleted.. It belonged to ** %s **. I can no longer alert you based off your role you can edit the class and set the role by using class edit.", role.getName(), classroom.getName()).queue();
                                              LOGGER.info("{} has been warned in the default channel", guild.getName());
                                              return;
                                        }

                                        LOGGER.warn("{} has no default or system channel!", event.getGuild().getName());

                                        if (user != null)
                                        {
                                              user.openPrivateChannel()
                                                      .flatMap(privateMessage -> privateMessage.sendMessageFormat
                                                              ("** %s ** has deleted.. It belonged to ** %s **. I can no longer alert you based off your role you can edit the class and set the role by using class edit.", role.getName(), classroom.getName())).queue();

                                              LOGGER.info("{} has been warned in their PM's", user.getAsMention());
                                              return;

                                        }

                                        LOGGER.warn("{} is owner-less.. Nothing I can do to alert", event.getGuild().getName());
                                  });
                    });
      }
}
