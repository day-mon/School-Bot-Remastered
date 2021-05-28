package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.util.List;

public class SchoolEdit extends Command
{
      public SchoolEdit(Command parent)
      {
            super(parent, "Edits a school", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            School school;
            JDA jda = event.getJDA();
            List<School> schools = event.getGuildSchools();

            if (schools.isEmpty())
            {
                  Embed.error(event, "** %s ** has no schools in it", event.getGuild().getName());
            }
            else if (schools.size() == 1)
            {
                  school = schools.get(0);
                  event.sendMessage("** %s ** has been selected because it is the only school. Would you want to continue?", school.getName());
                  jda.addEventListener(new SchoolEditStateMachine(event, school));

            }
            else
            {
                  event.sendMessage("Please give me the school you want to edit in page numbers");
                  event.getAsPaginatorWithPageNumbers(schools);
                  jda.addEventListener(new SchoolEditStateMachine(event, schools));

            }

      }


      public static class SchoolEditStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private final CommandEvent commandEvent;
            private int state = 0;
            private School school;
            private String updateColumn = "";
            private List<School> schoolList;

            public SchoolEditStateMachine(CommandEvent event, School school)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.commandEvent = event;
                  this.school = school;
                  state = 2;
            }

            public SchoolEditStateMachine(CommandEvent event, List<School> school)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.commandEvent = event;
                  this.schoolList = school;
                  state = 1;
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
                              }

                              int index = Integer.parseInt(message);

                              if (!Checks.between(index, schoolList.size()))
                              {
                                    Embed.error(event, "%d is not between 1 - %d. Try again", index, schoolList.size());
                              }

                              school = schoolList.get(index);

                              channel.sendMessage(school.getAsEmbed(commandEvent.getSchoolbot())).queue();
                              channel.sendMessageFormat("** %s ** has been selected, Would you like to continue?", school.getName()).queue();
                              state = 2;
                        }

                        case 2 -> {

                              if (!message.equalsIgnoreCase("yes") && !message.equalsIgnoreCase("y") && !message.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay aborting...").queue();
                                    jda.removeEventListener(this);
                              }

                              channel.sendMessageFormat("What attribute of ** %s ** would you like to edit", school.getName()).queue();
                              channel.sendMessage("""
                                        ```
                                      1. Name
                                      2. School URL
                                      3. Email Suffix
                                      4. Role```""").queue();
                              state = 3;
                        }

                        case 3 -> {
                              String content = message.toLowerCase().trim();

                              if (updateColumn.equalsIgnoreCase("N/A"))
                              {
                                    Embed.error(event, "** %s ** is not a valid choice please return again");
                                    return;
                              }

                              if (content.contains("1") || content.contains("name"))
                              {
                                    updateColumn = "name";
                                    channel.sendMessage("Please send me the name you would like the school to be").queue();
                              }
                              else if (content.contains("url") || content.contains("2"))
                              {
                                    updateColumn = "url";
                                    channel.sendMessage("Please send me the new school URL you would like the school to be").queue();
                              }
                              else if (content.contains("suffix") || content.contains("email") || content.contains("3"))
                              {
                                    updateColumn = "email_suffix";
                                    channel.sendMessage("Give me the email suffix you would like").queue();
                              }
                              else if (content.contains("role") || content.contains("4"))
                              {
                                    updateColumn = "role_id";
                                    channel.sendMessage("Please mention the role you would like the school to change to").queue();
                              }
                              else
                              {
                                    Embed.error(event, "** %s ** is not a valid entry");
                                    return;
                              }
                              state = 4;
                        }

                        case 4 -> {
                              switch (updateColumn)
                              {
                                    case "name" -> {
                                          if (Checks.isNumber(message))
                                          {
                                                Embed.error(event, "School names cannot contain numbers.. Try again");
                                                return;
                                          }

                                          boolean duplicateSchool = commandEvent.schoolExist(message);

                                          if (duplicateSchool)
                                          {
                                                Embed.error(event, "** %s ** already exist as a school.. Please try again with a different name");
                                                return;
                                          }
                                          commandEvent.updateSchool(commandEvent, new SchoolUpdateDTO(school, updateColumn, message));
                                    }
                                    case "url" -> {
                                          // TODO: Add valid url checks..
                                          commandEvent.updateSchool(commandEvent, new SchoolUpdateDTO(school, updateColumn, message));
                                    }
                                    case "email_suffix" -> {
                                          // TODO: Add valid email checks...
                                          commandEvent.updateSchool(commandEvent, new SchoolUpdateDTO(school, updateColumn, message));
                                    }
                                    case "role_id" -> {
                                          Message eventMessage = event.getMessage();

                                          if (eventMessage.getMentionedRoles().isEmpty() && !message.equalsIgnoreCase("0"))
                                          {
                                                Embed.error(event, "You did not mention any roles, Try again!");
                                                return;
                                          }

                                          Long roleID = message.equalsIgnoreCase("0") ? 0L : eventMessage.getMentionedRoles().get(0).getIdLong();

                                          if (roleID != 0 && roleID == school.getRoleID())
                                          {
                                                Embed.error(event, "%s, is already %s role", jda.getRoleById(school.getRoleID()).getAsMention(), school.getName());
                                                jda.removeEventListener(this);
                                                return;
                                          }

                                          if (roleID == 0 && school.getRoleID() == 0)
                                          {
                                                Embed.error(event, "%s already has no role.", school.getName());
                                                jda.removeEventListener(this);
                                                return;
                                          }

                                          commandEvent.updateSchool(commandEvent, new SchoolUpdateDTO(school, updateColumn, roleID));
                                    }
                              }
                              Embed.success(event, "** %s ** has been successfully been updated to ** %s ** ", updateColumn.replace("_", " "), message);
                              jda.removeEventListener(this);
                        }

                  }
            }
      }


      public static record SchoolUpdateDTO(School school, String updateColumn, Object value)
      {
      }


}
