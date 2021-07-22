package schoolbot.listeners;

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
      private final Logger logger = LoggerFactory.getLogger(RoleListener.class);

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
            var role = event.getRole();

            if (role.getName().equalsIgnoreCase(selfUser.getName()))
            {
                  logger.info("Most likely the RoleDeleteEvent. The role being deleted is the bots role");
                  return;
            }

            guild.retrieveAuditLogs()
                    .type(ActionType.ROLE_DELETE)
                    .limit(1)
                    .queue(roleDelete ->
                    {
                          var roleDeleteUser = roleDelete.get(0).getUser();


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
                                              logger.info("{} has been warned in the default channel", guild.getName());
                                              return;
                                        }

                                        logger.warn("{} has no default or system channel!", event.getGuild().getName());

                                        if (user != null)
                                        {
                                              user.openPrivateChannel()
                                                      .flatMap(privateMessage -> privateMessage.sendMessageFormat
                                                              ("** %s ** has deleted.. It belonged to ** %s **. I can no longer alert you based off your role you can edit the class and set the role by using class edit.", role.getName(), classroom.getName())).queue();

                                              logger.info("{} has been warned in their PM's", user.getAsMention());
                                              return;

                                        }

                                        logger.warn("{} is owner-less.. Nothing I can do to alert", event.getGuild().getName());
                                  });
                    }, failure -> logger.warn("{} does not have access to audit logs to check who removed the role.", selfUser.getName()));
      }
}
