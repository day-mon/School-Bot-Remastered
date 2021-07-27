package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
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
                  EmbedUtils.bConfirmation(values.getCommandEvent(), "Are you sure you want to remove **%s**", (buttonClickEvent) ->
                  {
                        var choice = buttonClickEvent.getComponentId();

                        if (choice.equals("confirm"))
                        {
                              event.removeSchool(school);
                              EmbedUtils.success(event, "Removed [** %s **] successfully", school.getName());
                        }
                        else if (choice.equals("abort"))
                        {
                              EmbedUtils.abort(event);
                        }

                  }, school.getName());
            }

            if (processedList == 2)
            {
                  var eventWaiter = event.getSchoolbot().getEventWaiter();

                  eventWaiter.waitForEvent(MessageReceivedEvent.class,
                          messageReceivedEvent -> messageReceivedEvent.getChannel().getIdLong() == event.getChannel().getIdLong()
                                                  && messageReceivedEvent.getAuthor().getIdLong() == event.getMember().getIdLong()
                                                  && messageReceivedEvent.getMessage().getContentRaw().chars().allMatch(Character::isDigit)
                                                  && Checks.between(Integer.parseInt(messageReceivedEvent.getMessage().getContentRaw()), schools.size()),
                          onEvent ->
                          {
                                var index = Integer.parseInt(onEvent.getMessage().getContentRaw()) - 1;

                                var school = schools.get(index);


                                EmbedUtils.bConfirmation(values.getCommandEvent(), "Are you sure you want to remove **%s**", (buttonClickEvent) ->
                                {
                                      var choice = buttonClickEvent.getComponentId();

                                      if (choice.equals("confirm"))
                                      {
                                            values.getCommandEvent().removeSchool(school);
                                            EmbedUtils.success(event, "Removed [** %s **] successfully", school.getName());
                                      }
                                      else if (choice.equals("abort"))
                                      {
                                            EmbedUtils.abort(event);
                                      }
                                }, school.getName());
                          });
            }
      }
}
