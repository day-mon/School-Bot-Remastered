package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

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
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            School school;
            Professor professor;
            Schoolbot schoolbot = event.getSchoolbot();
            JDA jda = event.getJDA();


            if (schools.isEmpty())
            {
                  Embed.error(event, "** %s ** has no schools with professors to edit", event.getGuild().getName());
            }
            else if (schools.size() == 1)
            {
                  school = schools.get(0);

                  List<Professor> professors = school.getProfessorList();

                  if (professors.isEmpty())
                  {
                        Embed.error(event, "** %s ** has no professors", school.getName());
                  }
                  else if (professors.size() == 1)
                  {
                        professor = professors.get(0);

                        event.sendMessage("""
                                ** %s ** is the only professor that is available.. Would you like to continue?
                                What attribute would you like to edit
                                                        
                                      ```1. First Name
                                         2. Last Name
                                         3. Email Prefix```
                                """, professor.getFullName());
                        jda.addEventListener(new ProfessorEditStateMachine(event, professor, 4));
                  }
                  else
                  {
                        event.sendAsPaginatorWithPageNumbers(professors);
                        event.sendMessage("Please choose a professor that you choose to edit based off page number");
                        jda.addEventListener(new ProfessorEditStateMachine(event, professors, schools, 3));
                  }
            }
            else
            {
                  event.sendMessage("There are multiple schools with editable professors.. Please select the page number of the school you want!");
                  event.sendAsPaginatorWithPageNumbers(schools);
                  jda.addEventListener(new ProfessorEditStateMachine(event, null, schools, 1));
            }
      }

      public static class ProfessorEditStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private final CommandEvent commandEvent;
            private int state;
            private School school;
            private String updateColumn = "";
            private List<School> schoolList;
            private List<Professor> professors;
            private Professor professor;

            public ProfessorEditStateMachine(CommandEvent event, Professor professor, int stateToGoto)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.commandEvent = event;
                  this.professor = professor;
                  this.school = professor.getProfessorsSchool();
                  state = stateToGoto;
            }

            public ProfessorEditStateMachine(CommandEvent event, List<Professor> professors, List<School> schools, int stateToGoto)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.commandEvent = event;
                  this.schoolList = schools;
                  this.professors = professors;
                  state = stateToGoto;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().getIdLong() != authorID) return;
                  if (event.getChannel().getIdLong() != channelID) return;

                  String message = event.getMessage().getContentRaw();
                  MessageChannel channel = event.getChannel();
                  JDA jda = event.getJDA();


                  if (message.equalsIgnoreCase("stop") || message.equalsIgnoreCase("exit"))
                  {
                        channel.sendMessage("Okay aborting..").queue();
                        jda.removeEventListener(this);
                        return;
                  }

                  switch (state)
                  {
                        case 1 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.notANumberError(event, message);
                                    return;
                              }

                              int index = Integer.parseInt(message);

                              if (!Checks.between(index, schoolList.size()))
                              {
                                    Embed.error(event, "%d is not between 1 - %d. Try again", index, schoolList.size());
                                    return;
                              }

                              school = schoolList.get(index - 1);
                              professors = school.getProfessorList();

                              channel.sendMessageFormat("** %s ** has been selected, Would you like to continue?", school.getName()).queue();
                              state = 2;
                        }

                        case 2 -> {
                              if (!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("y") && !message.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay aborting...").queue();
                                    jda.removeEventListener(this);
                              }

                              commandEvent.sendAsPaginatorWithPageNumbers(professors);
                              channel.sendMessage("Please give me the page number associated with the professor you want to edit").queue();

                              state = 3;
                        }

                        case 3 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.notANumberError(event, message);
                                    return;
                              }

                              int index = Integer.parseInt(message);

                              if (!Checks.between(index, professors.size()))
                              {
                                    Embed.error(event, "%d is not between 1 - %d. Try again", index, professors.size());
                                    return;
                              }

                              professor = professors.get(index - 1);

                              channel.sendMessageFormat("What attribute of ** %s ** would you like to edit", professor.getFullName()).queue();
                              channel.sendMessage("""
                                      ```1. First Name
                                         2. Last Name
                                         3. Email Prefix```
                                       """).queue();
                              state = 4;
                        }


                        case 4 -> {
                              String content = message.toLowerCase().trim();


                              if (updateColumn.equalsIgnoreCase("N/A"))
                              {
                                    Embed.error(event, "** %s ** is not a valid choice please return again");
                                    return;
                              }

                              evaluateChoice(content, event);

                              state = 5;
                        }

                        case 5 -> evaluateColumn(message, event);

                  }
            }

            private void evaluateChoice(String content, GuildMessageReceivedEvent event)
            {
                  MessageChannel channel = event.getChannel();
                  if (content.contains("1") || content.contains("first"))
                  {
                        updateColumn = "first_name";
                        channel.sendMessage("Please send me the new first name you would like for this professor").queue();
                  }
                  else if (content.contains("last") || content.contains("2"))
                  {
                        updateColumn = "last_name";
                        channel.sendMessage("Please send me the new last name you would like for this professor").queue();
                  }
                  else if (content.contains("prefix") || content.contains("email") || content.contains("3"))
                  {
                        updateColumn = "email_suffix";
                        channel.sendMessage("Give me the email prefix you would like for this professor").queue();
                  }
                  else
                  {
                        Embed.error(event, "** %s ** is not a valid entry");
                  }
            }

            private void evaluateColumn(String message, GuildMessageReceivedEvent event)
            {
                  JDA jda = event.getJDA();
                  switch (updateColumn)
                  {
                        case "first_name", "last_name" -> {
                              if (Checks.isNumber(message))
                              {
                                    Embed.error(event, "Names cannot contain numbers.. Try again");
                                    return;
                              }
                              commandEvent.updateProfessor(commandEvent, new DatabaseDTO(professor, updateColumn, message));
                        }
                        case "email_prefix" -> commandEvent.updateProfessor(commandEvent, new DatabaseDTO(professor, updateColumn, message));

                        default -> {
                              Embed.error(event, "** %s ** is not a valid response", message);
                              return;
                        }
                  }
                  Embed.success(event, "** %s ** has been successfully been updated to ** %s ** ", updateColumn.replace("_", " "), message);
                  jda.removeEventListener(this);
            }
      }
}
