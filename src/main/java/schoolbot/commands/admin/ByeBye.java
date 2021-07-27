package schoolbot.commands.admin;

import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.Classroom;
import schoolbot.util.DatabaseUtils;
import schoolbot.util.EmbedUtils;

import java.util.List;

public class ByeBye extends Command
{
      private final Logger LOGGER = LoggerFactory.getLogger(ByeBye.class);

      public ByeBye()
      {
            super("Leaves the targets discord and deletes all association with the discord", "[none]", 0);
            addCalls("byebye", "bye", "bai");
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS);
            addCommandPrerequisites("Be owner of server");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var channel = event.getChannel();

            var ownerId = event.getGuild().getOwnerIdLong();
            var memberId = event.getMember().getIdLong();

            if (ownerId != memberId)
            {
                  EmbedUtils.error(event, "You are not an owner. You need to be an owner to call this command");
                  return;
            }


            EmbedUtils.bConfirmation(event, "Hello %s.. calling this command means you will remove everything I created as well as me leaving the server. Are you sure you want to do this?", (buttonClickEvent) ->
            {
                  var choice = buttonClickEvent.getComponentId();

                  if (choice.equals("confirm"))
                  {
                        removeAllRoles(event);
                        removeAllChannels(event);
                        DatabaseUtils.removeAllGuildOccurrences(event.getSchoolbot(), event.getGuild().getIdLong());

                        channel.sendMessage("Goodbye!").queue(__ -> event.getGuild().leave().queue());
                  }
                  else if (choice.equals("abort"))
                  {
                        EmbedUtils.abort(event);
                  }
            }, event.getUser().getAsMention());

      }

      private void removeAllChannels(CommandEvent event)
      {
            event.getGuildClasses()
                    .stream()
                    .map(Classroom::getChannelID)
                    .forEach(textChannel ->
                    {
                          var jda = event.getJDA();

                          var channel = jda.getTextChannelById(textChannel);

                          if (channel != null)
                          {
                                channel.delete().queue(null,
                                        channelRemovalFailure -> LOGGER.error("Cannot delete a TextChannel in the removal process. The TextChannel ID is {}", textChannel, channelRemovalFailure));
                          }

                    });
      }

      private void removeAllRoles(CommandEvent event)
      {
            event.getGuildClasses()
                    .stream()
                    .map(Classroom::getRoleID)
                    .forEach(roleId ->
                    {
                          var jda = event.getJDA();

                          var role = jda.getRoleById(roleId);

                          if (role != null)
                          {
                                role.delete().queue(null,
                                        roleRemovalFailure -> LOGGER.error("Cannot delete a Role in the removal process. The Role ID is {}", roleId, roleRemovalFailure));
                          }
                    });


      }
}
