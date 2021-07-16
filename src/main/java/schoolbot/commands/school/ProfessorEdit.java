package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class ProfessorEdit extends Command
{
      public ProfessorEdit(Command parent)
      {
            super(parent, "Edits a professor to the server list", "[professor name] [attribute to edit] [edit]", 0);
            addPermissions(Permission.ADMINISTRATOR);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            StateMachineValues values = new StateMachineValues(event);
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schools);
            var jda = event.getJDA();


            var returnCode = Processor.processGenericList(values, schools, School.class);

            if (returnCode == 0)
            {
                  return;
            }

            if (returnCode == 1)
            {
                  var school = values.getSchool();
                  var professorReturnCode = Processor.processGenericList(values, school.getProfessorList(), Professor.class);

                  if (professorReturnCode == 1)
                  {
                        var professor = values.getProfessor();
                        event.sendMessage("""
                                ** %s ** is the only professor that is available.. Would you like to continue?
                                What attribute would you like to edit
                                                        
                                      ```1. First Name
                                         2. Last Name
                                         3. Email Prefix```
                                """, professor.getFullName());
                        values.setState(4);
                  }
                  else if (professorReturnCode == 2)
                  {
                        values.setState(3);
                  }

            }

            jda.addEventListener(new ProfessorEditStateMachine(values));

      }

      private static class ProfessorEditStateMachine extends ListenerAdapter implements StateMachine
      {

            private String updateColumn = "";
            private final StateMachineValues values;
            private boolean selectionEventGoneThrough = false;

            private ProfessorEditStateMachine(StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);

                  if (!requirementsMet)
                  {
                        return;
                  }

                  String message = event.getMessage().getContentRaw();
                  var channel = event.getChannel();
                  var jda = event.getJDA();
                  int state = values.getState();


                  switch (state)
                  {
                        case 1 -> {

                              var success = Processor.validateMessage(values, values.getSchoolList());

                              if (!success)
                              {
                                    return;
                              }

                              var school = values.getSchool();

                              channel.sendMessageFormat("** %s ** has been selected, Would you like to continue?", school.getName()).queue();
                        }

                        case 2 -> {
                              if (!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("y") && !message.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay aborting...").queue();
                                    jda.removeEventListener(this);
                              }

                              var cmdEvent = values.getCommandEvent();

                              cmdEvent.sendAsPaginatorWithPageNumbers(values.getProfessorList());
                              channel.sendMessage("Please give me the page number associated with the professor you want to edit").queue();

                              values.incrementMachineState();

                        }

                        case 3 -> {

                              if (selectionEventGoneThrough)
                              {
                                    EmbedUtils.warn(event, "You need to select an option from the selection menu!");
                                    return;
                              }

                              var success = Processor.validateMessage(values, values.getProfessorList());
                              values.setState(3);

                              if (!success)
                              {
                                    return;
                              }


                              var professor = values.getProfessor();


                              if (professor.getFullName().equalsIgnoreCase("staff unknown"))
                              {
                                    EmbedUtils.error(event, "You cannot edit this professor.. Aborting");
                                    jda.removeEventListener(this);
                                    return;
                              }


                              var commandEvent = values.getCommandEvent();


                              List<SelectOption> selectOptions = List.of(
                                      SelectOption.of("First Name", "firstName"),
                                      SelectOption.of("Last Name", "lastName"),
                                      SelectOption.of("Email Prefix", "emailPrefix")
                              );

                              selectionEventGoneThrough = true;


                              commandEvent.sendMenuAndAwait(String.format("Which attribute of %s would you like to edit", professor.getFullName()), selectOptions, (selectionMenuEvent) ->
                              {
                                    var updateChosen = selectionMenuEvent.getValues().get(0);

                                    switch (updateChosen)
                                    {
                                          case "firstName" -> {
                                                updateColumn = "first_name";
                                                channel.sendMessage("Please send me the new first name you would like for this professor").queue();
                                          }

                                          case "lastName" -> {
                                                updateColumn = "last_name";
                                                channel.sendMessage("Please send me the new last name you would like for this professor").queue();
                                          }

                                          case "emailPrefix" -> {
                                                updateColumn = "email_prefix";
                                                channel.sendMessage("Give me the email prefix you would like for this professor").queue();
                                          }
                                    }

                                    values.incrementMachineState();
                              });

                        }

                        case 4 -> evaluateColumn(values);

                  }
            }



            private void evaluateColumn(StateMachineValues values)
            {
                  var jda = values.getJda();
                  var event = values.getMessageReceivedEvent();
                  var message = event.getMessage().getContentRaw();
                  var commandEvent = values.getCommandEvent();

                  var professor = values.getProfessor();

                  switch (updateColumn)
                  {
                        case "first_name", "last_name" -> {
                              if (Checks.isNumber(message))
                              {
                                    EmbedUtils.error(event, "Names cannot contain numbers.. Try again");
                                    return;
                              }
                              commandEvent.updateProfessor(new DatabaseDTO(professor, updateColumn, message));
                        }
                        case "email_prefix" -> commandEvent.updateProfessor(new DatabaseDTO(professor, updateColumn, message));

                        default -> {
                              EmbedUtils.error(event, "**%s** is not a valid response", message);
                              return;
                        }
                  }
                  EmbedUtils.success(event, "** %s ** has been successfully been updated to ** %s ** ", updateColumn.replace("_", " "), message);
                  jda.removeEventListener(this);
            }
      }
}
