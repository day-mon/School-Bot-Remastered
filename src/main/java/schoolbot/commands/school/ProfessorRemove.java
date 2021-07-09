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
            List<School> schoolList = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schoolList);

            var processedList = Processor.processGenericList(values, schoolList, School.class);

            if (processedList == 0)
            {
                  return;
            }

            if (processedList == 1)
            {
                  var school = values.getSchool();

                  EmbedUtils.information(event, "** %s ** has been selected because it is the only school with professors that are available to delete", school.getName());

                  var processedProfessorList = Processor.processGenericList(values, values.getProfessorList(), Professor.class);

                  if (processedProfessorList == 1)
                  {
                        var professor = values.getProfessor();

                        EmbedUtils.information(event, "** %s ** is the only professor available to delete. Are you sure you want to delete them?", professor.getFullName());

                        values.setState(4);
                  }
                  else if (processedProfessorList == 2)
                  {
                        values.setState(3);
                  }
            }

            jda.addEventListener(new ProfessorRemoveStateMachine(values));

      }


      private static class ProfessorRemoveStateMachine extends ListenerAdapter implements StateMachine
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
                  var channel = event.getChannel();
                  var commandEvent = values.getCommandEvent();

                  String message = event.getMessage().getContentRaw();

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

                              var professorList = values.getProfessorList()
                                      .stream()
                                      .filter(professor -> professor.getListOfClasses().isEmpty())
                                      .collect(Collectors.toList());

                              values.setProfessorList(professorList);

                              if (professorList.size() == 1)
                              {
                                    var professor = professorList.get(0);
                                    values.setProfessor(professor);
                                    channel.sendMessageFormat("** %s ** is the only professor available to delete at this time.. Would you like to delete them?", professor.getFullName()).queue();

                                    values.setState(4);
                                    return;
                              }

                              channel.sendMessage("Thank you for that.. Please select from a professor you would like to remove").queue();
                              commandEvent.sendAsPaginatorWithPageNumbers(professorList);

                        }

                        case 2 -> {
                              List<Professor> professorList = values.getProfessorList()
                                      .stream()
                                      .filter(professors -> professors.getListOfClasses().isEmpty())
                                      .collect(Collectors.toList());
                              values.setProfessorList(professorList);

                              var listReturnCode = Processor.processGenericList(values, professorList, Professor.class);

                              if (listReturnCode == 0)
                              {
                                    jda.removeEventListener(this);
                                    return;
                              }

                              if (listReturnCode == 1)
                              {
                                    var professor = values.getProfessor();

                                    channel.sendMessageFormat("** %s ** is the only professor available to delete at this time.. Would you like to delete them?", professor.getFullName()).queue();
                                    values.setState(4);
                              }

                              // No need to check the listReturnCode == 2 because processGenericList handles that and increments state


                        }

                        case 3 -> {

                              var valid = Processor.validateMessage(values, values.getProfessorList());


                              if (!valid)
                              {
                                    return;
                              }

                              var professor = values.getProfessor();

                              channel.sendMessageFormat("Are you sure you want to delete Professor %s", professor.getLastName()).queue();
                        }

                        case 4 -> {
                              var professor = values.getProfessor();
                              if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y"))
                              {
                                    commandEvent.removeProfessor(professor);
                                    EmbedUtils.success(event, "Removed [** %s **] successfully", professor.getFullName());
                                    event.getJDA().removeEventListener(this);
                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("nah"))
                              {
                                    channel.sendMessage("Okay.. aborting..").queue();
                                    event.getJDA().removeEventListener(this);
                              }
                              else
                              {
                                    EmbedUtils.error(event, "[ ** %s ** ] is not a valid respond.. I will need a **Yes** OR a **No**", message);
                              }
                        }
                  }
            }
      }
}

