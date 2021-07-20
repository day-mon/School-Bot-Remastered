package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class SchoolRemove extends Command
{
      public SchoolRemove(Command parent)
      {
            super(parent, "Removes a school", "<none>", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> school.getClassroomList().isEmpty())
                    .filter(school -> school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schools);

            var processedList = Processor.processGenericList(values, schools, School.class);

            if (processedList == 0)
            {
                  return;
            }

            if (processedList == 1)
            {
                  var school = values.getSchool();
                  EmbedUtils.confirmation(values.getCommandEvent(), "Are you sure you want to remove **%s**", (messageReactionAddEvent) ->
                  {
                        var reactionEmote = messageReactionAddEvent.getReactionEmote().getName();

                        if (reactionEmote.equals(Emoji.CROSS_MARK.getUnicode()))
                        {
                              event.sendMessage("Okay.. aborting..");
                        }
                        else if (reactionEmote.equals(Emoji.WHITE_CHECK_MARK.getUnicode()))
                        {
                              values.getCommandEvent().removeSchool(school);
                              EmbedUtils.success(event, "Removed [** %s **] successfully", school.getName());
                        }
                  }, school.getName());
            }

            if (processedList == 2)
            {
                  var eventWaiter = event.getSchoolbot().getEventWaiter();

                  eventWaiter.waitForEvent(MessageReceivedEvent.class,
                          messageReceivedEvent -> messageReceivedEvent.getChannel().getIdLong() == event.getChannel().getIdLong()
                                                  && messageReceivedEvent.getMember().getIdLong() == event.getMember().getIdLong()
                                                  && messageReceivedEvent.getMessage().getContentRaw().chars().allMatch(Character::isDigit)
                                                  && Checks.between(Integer.parseInt(messageReceivedEvent.getMessage().getContentRaw()), schools.size()),
                          onEvent ->
                          {
                                var index = Integer.parseInt(onEvent.getMessage().getContentRaw()) - 1;

                                var school = schools.get(index);
                                var channel = onEvent.getChannel();

                                EmbedUtils.confirmation(values.getCommandEvent(), "Are you sure you want to remove **%s**", (messageReactionAddEvent) ->
                                {
                                      var reactionEmote = messageReactionAddEvent.getReactionEmote().getName();

                                      if (reactionEmote.equals(Emoji.CROSS_MARK.getUnicode()))
                                      {
                                            channel.sendMessage("Okay.. aborting..").queue();
                                      }
                                      else if (reactionEmote.equals(Emoji.WHITE_CHECK_MARK.getUnicode()))
                                      {
                                            values.getCommandEvent().removeSchool(school);
                                            EmbedUtils.success(event, "Removed [** %s **] successfully", school.getName());
                                      }
                                }, school.getName());
                          });
            }
      }
}
