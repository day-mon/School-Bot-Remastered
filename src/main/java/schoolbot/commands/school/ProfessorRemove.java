package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.util.List;
import java.util.stream.Collectors;

public class ProfessorRemove extends Command
{
      /**
       * @param parent
       */
      public ProfessorRemove(Command parent)
      {
            super(parent, "Removes a professor from the guild", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();

            List<School> schoolList = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());

            if (schoolList.isEmpty())
            {
                  Embed.error(event, "[ ** %s ** ] has no schools with deletable professors", event.getGuild().getName());
            }
            else if (schoolList.size() == 1)
            {
                  Embed.information(event, "** %s ** has been selected because this is the only school with professors", schoolList.get(0).getSchoolName());

                  List<Professor> professorList = schoolList.get(0).getProfessorList()
                          .stream()
                          .filter(professor -> professor.getListOfClasses().isEmpty())
                          .collect(Collectors.toList());

                  if (professorList.size() == 1)
                  {
                        event.sendMessage(professorList.get(0).getAsEmbed(event.getSchoolbot()));
                        event.sendMessage("This is the only professor would you like to delete?");
                        event.getJDA().addEventListener(new ProfessorRemoveStateMachine(event, schoolList, professorList, 4));
                  }
                  else
                  {
                        event.getAsPaginatorWithPageNumbers(professorList);
                        event.sendMessage("Please choose the page number of the professor you would like to remove");
                        event.getJDA().addEventListener(new ProfessorRemoveStateMachine(event, schoolList, professorList, 3));
                  }

            }
            else
            {
                  event.getAsPaginatorWithPageNumbers(schoolList);
                  event.sendMessage("Please give me the page number of the school you want to remove the professor from");
                  event.getJDA().addEventListener(new ProfessorRemoveStateMachine(event, schoolList, null, 1));
            }
      }


      public static class ProfessorRemoveStateMachine extends ListenerAdapter
      {
            private final long authorID, channelID;
            private final CommandEvent commandEvent;
            private final List<School> schoolList;
            private School school;
            private Professor professor;
            private List<Professor> professorList;
            private int state;


            public ProfessorRemoveStateMachine(CommandEvent commandEvent, List<School> school, List<Professor> professorList, int state)
            {
                  this.commandEvent = commandEvent;
                  this.schoolList = school;
                  this.professorList = professorList;
                  this.state = state;
                  this.channelID = commandEvent.getChannel().getIdLong();
                  this.authorID = commandEvent.getUser().getIdLong();
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().getIdLong() != authorID) return;
                  if (event.getChannel().getIdLong() != channelID) return;

                  String message = event.getMessage().getContentRaw();
                  MessageChannel channel = event.getChannel();

                  if (message.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Okay aborting... ").queue();
                        event.getJDA().removeEventListener(this);
                        return;
                  }

                  switch (state)
                  {
                        case 1 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, "** %s ** is not a number. Try again", message);
                                    return;
                              }

                              int page = Integer.parseInt(message);

                              if (!Checks.between(page, 1, schoolList.size()))
                              {
                                    Embed.error(event, "%d is not a number between 1 and %d. Try again", page, schoolList.size());
                                    return;
                              }

                              this.school = schoolList.get(page - 1);
                              this.professorList = school.getProfessorList();
                              state = 2;

                        }

                        case 2 -> {

                              List<Professor> professorList = this.professorList
                                      .stream()
                                      .filter(professors -> professors.getListOfClasses().isEmpty())
                                      .collect(Collectors.toList());
                              if (professorList.isEmpty())
                              {
                                    Embed.error(event, "There are no deletable professors, remove some of their classes first");
                                    event.getJDA().removeEventListener(this);
                              }
                              else if (professorList.size() == 1)
                              {
                                    channel.sendMessage(professorList.get(0).getAsEmbed(commandEvent.getSchoolbot())).queue();
                                    channel.sendMessage("This is the only professor would you like to delete?").queue();
                                    state = 4;
                              }
                              else
                              {
                                    commandEvent.getAsPaginatorWithPageNumbers(professorList);
                                    channel.sendMessage("Please choose the page number of the professor you would like to remove").queue();
                                    state = 3;
                              }
                        }

                        case 3 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, "** %s ** is not a number. Try again", message);
                                    return;
                              }

                              int page = Integer.parseInt(message);

                              if (!Checks.between(page, 1, schoolList.size()))
                              {
                                    Embed.error(event, "%d is not a number between 1 and %d. Try again", page, schoolList.size() + 1);
                                    return;
                              }

                              this.professor = professorList.get(page - 1);
                              channel.sendMessageFormat("Are you sure you want to delete Professor %s", professor.getLastName()).queue();
                              state = 4;
                        }

                        case 4 -> {
                              if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y"))
                              {
                                    commandEvent.removeProfessor(commandEvent, professor);
                                    Embed.success(event, "Removed [** %s **] successfully", this.professor.getFullName());
                                    event.getJDA().removeEventListener(this);
                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("nah"))
                              {
                                    channel.sendMessage("Okay.. aborting..").queue();
                                    event.getJDA().removeEventListener(this);
                              }
                              else
                              {
                                    Embed.error(event, "[ ** %s ** ] is not a valid respond.. I will need a **Yes** OR a **No**", message);
                              }
                        }
                  }

            }


      }

}

