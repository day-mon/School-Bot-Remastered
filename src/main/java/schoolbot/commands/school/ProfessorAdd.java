package schoolbot.commands.school;


import net.dv8tion.jda.api.entities.MessageChannel;
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
import schoolbot.util.Embed;
import schoolbot.util.Processor;

import java.util.List;

public class ProfessorAdd extends Command
{
      public ProfessorAdd(Command parent)
      {
            super(parent, "Adds a professor to the server list", "[none]", 0);
            addFlags(CommandFlag.DATABASE, CommandFlag.STATE_MACHINE_COMMAND);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            List<School> schools = event.getGuildSchools();

            if (schools.isEmpty())
            {
                  event.sendMessage("You have no schools to add this professor to... try using -school add first");
                  return;
            }
            event.sendMessage("To start.. Whats your professors first name:  ");
            event.getJDA().addEventListener(new ProfessorAddStateMachine(values));
      }


      public static class ProfessorAddStateMachine extends ListenerAdapter implements StateMachine
      {

            private final StateMachineValues values;


            public ProfessorAddStateMachine(StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }

            public void onGuildMessageReceived(GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);
                  var channel = event.getChannel();
                  var jda = event.getJDA();

                  String message = event.getMessage().getContentRaw();


                  if (!requirementsMet)
                  {
                        return;
                  }

                  int state = values.getState();


                  switch (state)
                  {
                        case 1 -> {
                              // Skips last name state
                              values.setProfessor(new Professor());
                              numCheck(message, channel);
                              channel.sendMessageFormat("Awesome! Thank you for that your professors first name is ** %s **", message).queue();


                              values.getProfessor().setFirstName(message);

                              channel.sendMessage("I will now need your professors last name: ").queue();
                              values.incrementMachineState();
                        }
                        case 2 -> {
                              numCheck(message, channel);
                              channel.sendMessageFormat("Thank you again. Your professor last name is ** %s **", message).queue();

                              values.getProfessor().setLastName(message);
                              var schoolList = values.getSchoolList();


                              if (schoolList.size() == 1)
                              {
                                    var school = schoolList.get(0);
                                    channel.sendMessageFormat("**%s** only has one school associated with it. I will automatically assign your professor to  **%s**", event.getGuild().getName(), school.getName()).queue();
                                    values.getProfessor().setProfessorsSchool(school);
                                    channel.sendMessage("Lastly, enter his email prefix: ").queue();
                                    values.setState(4);
                                    break;
                              }
                              var commandEvent = values.getCommandEvent();
                              channel.sendMessage("Moving on.. I will need you professors school.. Here is a list of all this servers schools! ").queue();
                              commandEvent.sendAsPaginatorWithPageNumbers(commandEvent.getGuildSchools());

                              values.incrementMachineState();
                        }
                        case 3 -> {
                              var success = Processor.validateMessage(values, values.getSchoolList());

                              if (!success)
                              {
                                    return;
                              }


                              var school = values.getSchool();

                              values.getProfessor().setProfessorsSchool(school);
                              channel.sendMessage("""
                                      Thank you for that! I will now need your professors email suffix
                                      For Ex: **litman**@cs.pitt.edu
                                      """).queue();

                        }
                        case 4 -> {
                              values.getProfessor().setEmailPrefix(message);
                              channel.sendMessage("Thank you.. Inserting all of the info into my database and Adding professor.").queue();

                              var commandEvent = values.getCommandEvent();
                              var professor = values.getProfessor();
                              var schoolbot = commandEvent.getSchoolbot();

                              if (!commandEvent.addProfessor(professor))
                              {
                                    Embed.error(event, "Could not add Professor %s", professor.getLastName());
                                    return;
                              }
                              channel.sendMessageEmbeds(professor.getAsEmbed(schoolbot)).queue();

                              jda.removeEventListener(this);
                        }
                  }
            }

            private void numCheck(String content, MessageChannel channel)
            {
                  if (content.chars().anyMatch(Character::isDigit))
                  {
                        channel.sendMessage("Professors fields cannot contain numbers!").queue();
                  }
            }

      }
}
