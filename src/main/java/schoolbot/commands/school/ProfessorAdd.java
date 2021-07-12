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
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
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


      private static class ProfessorAddStateMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;


            private ProfessorAddStateMachine(StateMachineValues values)
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

                              var processedList = Processor.processGenericListWithoutMessageSend(values, values.getSchoolList(), School.class);

                              if (processedList == 1)
                              {
                                    channel.sendMessageFormat("**%s** only has one school associated with it. I will automatically assign your professor to  **%s**", event.getGuild().getName(), values.getSchool().getName()).queue();
                                    values.getProfessor().setProfessorsSchool(values.getSchool());
                                    channel.sendMessageFormat("Lastly enter Professor %s's email prefix \n Ex: **jones**@pitt.edu", values.getProfessor().getFirstName()).queue();
                                    values.setState(4);
                                    break;
                              }


                              values.getCommandEvent().sendSelfDeletingMessage("Each professor needs a school. So choose a school from the corresponding pages!");
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
                                      Thank you for your professors school! I will now need their email prefix!
                                      For Ex: **jones**@pitt.edu
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
                                    EmbedUtils.error(event, "Could not add Professor %s", professor.getLastName());
                                    return;
                              }
                              channel.sendMessageEmbeds(professor.getAsEmbed(schoolbot))
                                      .append("Professor successfully added to ")
                                      .append(event.getGuild().getName())
                                      .queue();

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
