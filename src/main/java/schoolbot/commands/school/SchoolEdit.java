package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.util.List;

public class SchoolEdit extends Command
{
      public SchoolEdit(Command parent)
      {
            super(parent, "Edits a school", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();
            var schoolResponse = Processor.processGenericList(values, event.getGuildSchools(), School.class);

            if (schoolResponse == 0)
            {
                  return;
            }

            if (schoolResponse == 1)
            {
                  event.sendMessage("This school has been selected because it is the only school. Would you want to continue?");
                  values.setState(2);
            }

            jda.addEventListener(new SchoolEditStateMachine(values));
      }


      private static class SchoolEditStateMachine extends ListenerAdapter implements StateMachine
      {

            private String updateColumn = "";
            private final StateMachineValues values;
            private boolean selectionEventGoneThrough = false;

            private SchoolEditStateMachine(StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);
                  var channel = event.getChannel();
                  var jda = event.getJDA();
                  int state = values.getState();

                  if (!requirementsMet)
                  {
                        return;
                  }


                  String message = event.getMessage().getContentRaw();


                  switch (state)
                  {
                        case 1 -> {

                              var validation = Processor.validateMessage(event, values.getSchoolList());

                              if (validation == null)
                              {
                                    return;
                              }

                              values.setSchool(validation);

                              channel.sendMessageFormat("** %s ** has been selected, Would you like to continue?", validation.getName()).queue();
                              values.incrementMachineState();
                        }

                        case 2 -> {

                              if (selectionEventGoneThrough)
                              {
                                    EmbedUtils.warn(event, "You need to select an option from the selection menu!");
                                    return;
                              }

                              if (!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("y") && !message.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay aborting...").queue();
                                    jda.removeEventListener(this);
                              }

                              var school = values.getSchool();
                              var commandEvent = values.getCommandEvent();

                              List<SelectOption> selectOptionList = List.of(
                                      SelectOption.of("Name", "name"),
                                      SelectOption.of("School URL", "url"),
                                      SelectOption.of("Email Suffix", "suffix"),
                                      SelectOption.of("Role", "role")
                              );

                              commandEvent.sendMenuAndAwait(String.format("Which attribute of %s would you like to edit", school.getName()), selectOptionList, (selectionMenuEvent) ->
                              {
                                    var updateChosen = selectionMenuEvent.getValues().get(0);

                                    switch (updateChosen)
                                    {
                                          case "name" -> {
                                                updateColumn = "name";
                                                channel.sendMessage("Please send me the name you would like the school to be").queue();
                                          }

                                          case "url" -> {
                                                updateColumn = "url";
                                                channel.sendMessage("Please send me the new school URL you would like the school to be").queue();
                                          }

                                          case "emailPrefix" -> {
                                                updateColumn = "email_suffix";
                                                channel.sendMessage("Give me the email suffix you would like").queue();
                                          }

                                          case "role" -> {
                                                updateColumn = "role_id";
                                                channel.sendMessage("Please mention the role you would like the school to change to").queue();
                                          }
                                    }

                                    values.incrementMachineState();
                              });

                              selectionEventGoneThrough = true;
                        }


                        case 3 -> evaluateColumn(values);

                  }
            }


            private void evaluateColumn(StateMachineValues values)
            {
                  var jda = values.getJda();
                  var event = values.getCommandEvent();
                  var school = values.getSchool();
                  String message = values.getMessageReceivedEvent().getMessage().getContentRaw();

                  switch (updateColumn)
                  {
                        case "name" -> {
                              if (Checks.isNumber(message))
                              {
                                    EmbedUtils.error(event, "School names cannot contain numbers.. Try again");
                                    return;
                              }

                              boolean duplicateSchool = event.schoolExist(message);

                              if (duplicateSchool)
                              {
                                    EmbedUtils.error(event, "** %s ** already exist as a school.. Please try again with a different name");
                                    return;
                              }

                              event.updateSchool(new DatabaseDTO(school, updateColumn, message));
                        }
                        case "url", "email_suffix" -> event.updateSchool(new DatabaseDTO(school, updateColumn, message));
                        case "role_id" -> {
                              Message eventMessage = event.getMessage();

                              if (eventMessage.getMentionedRoles().isEmpty() && !message.equalsIgnoreCase("0"))
                              {
                                    EmbedUtils.error(event, "You did not mention any roles, Try again!");
                                    return;
                              }

                              long roleID = message.equalsIgnoreCase("0") ? 0L : eventMessage.getMentionedRoles().get(0).getIdLong();

                              if (roleID != 0 && roleID == school.getRoleID())
                              {
                                    EmbedUtils.error(event, "%s, is already %s role", jda.getRoleById(school.getRoleID()).getAsMention(), school.getName());
                                    jda.removeEventListener(this);
                                    return;
                              }

                              if (roleID == 0 && school.getRoleID() == 0)
                              {
                                    EmbedUtils.error(event, "%s already has no role.", school.getName());
                                    jda.removeEventListener(this);
                                    return;
                              }

                              event.updateSchool(new DatabaseDTO(school, updateColumn, roleID));
                        }
                        default -> EmbedUtils.error(event, "** %s ** is not a valid response", message);
                  }
                  EmbedUtils.success(event, "** %s ** has been successfully been updated to ** %s ** ", updateColumn.replace("_", " "), message);
                  jda.removeEventListener(this);
            }
      }
}
