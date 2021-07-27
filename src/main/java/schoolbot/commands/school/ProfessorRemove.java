package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class ProfessorRemove extends Command
{
      public ProfessorRemove(Command parent)
      {
            super(parent, "Removes a professor from the guild", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();

            values.setSchoolList(
                    event.getGuildSchools()
                            .stream()
                            .filter(School::hasProfessors)
                            .collect(Collectors.toList())
            );

            var processedList = Processor.processGenericListWithSendingList(values, values.getSchoolList(), School.class);

            if (processedList == 0)
            {
                  return;
            }

            if (processedList == 1)
            {

                  values.setProfessorList(
                          values.getProfessorList().stream()
                                  .filter(professor -> professor.getListOfClasses().isEmpty())
                                  .collect(Collectors.toList())
                  );

                  var school = values.getSchool();

                  event.sendSelfDeletingMessageFormat("** %s ** has been selected because it is the only school with professors that are available to delete", school.getName());

                  var processedProfessorList = Processor.processGenericList(values, values.getProfessorList(), Professor.class);

                  if (processedProfessorList == 1)
                  {
                        sendConfirmationMessage(values);
                        return;
                  }
                  else if (processedProfessorList == 2)
                  {
                        values.setState(2);
                  }
            }

            jda.addEventListener(new ProfessorRemoveStateMachine(values));

      }


      private class ProfessorRemoveStateMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;

            private ProfessorRemoveStateMachine(@NotNull StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);
                  var jda = event.getJDA();

                  if (!requirementsMet)
                  {
                        return;
                  }

                  int state = values.getState();

                  switch (state)
                  {
                        case 1 -> {
                              var valid = Processor.validateMessage(values, values.getSchoolList());

                              if (!valid)
                              {
                                    return;
                              }

                              var processedList = Processor.processGenericList(values, values.getProfessorList(), Professor.class);


                              if (processedList == 1)
                              {
                                    sendConfirmationMessage(values);
                                    jda.removeEventListener(this);
                              }
                        }

                        case 2 -> {

                              var valid = Processor.validateMessage(values, values.getProfessorList());

                              if (!valid)
                              {
                                    return;
                              }


                              sendConfirmationMessage(values);
                              jda.removeEventListener(this);
                        }
                  }
            }
      }

      private void sendConfirmationMessage(StateMachineValues values)
      {
            var event = values.getCommandEvent();
            var professor = values.getProfessor();

            EmbedUtils.bConfirmation(event, "Are you sure you would like to remove **%s**", (buttonClickEvent) ->
            {
                  var choice = buttonClickEvent.getComponentId();

                  if (choice.equals("confirm"))
                  {
                        event.removeProfessor(professor);
                        EmbedUtils.success(event, "Removed **%s** successfully", professor.getFullName());
                  }
                  else if (choice.equals("abort"))
                  {
                        EmbedUtils.abort(event);
                  }
            }, professor.getFullName());
      }
}

