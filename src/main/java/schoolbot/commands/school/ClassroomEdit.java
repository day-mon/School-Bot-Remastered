package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.*;

import java.sql.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ClassroomEdit extends Command
{

      private static final String UPDATE_MENU = """
              What attribute of ** %s ** would you like to edit
              ```
                    1. Name
                    2. Description
                    3. Professor
                    4. Time
                    5. Start Date
                    6. End Date
                    7. Number
                    8. Role
                    9. Channel
              ```
              """;

      public ClassroomEdit(Command parent)
      {
            super(parent, "Edits an classroom", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            List<School> schoolList = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getClassroomList().isEmpty())
                    .collect(Collectors.toList());

            var jda = event.getJDA();
            var guild = event.getGuild();
            var channel = event.getChannel();

            Classroom classroom = Checks.messageSentFromClassChannel(event);

            if (classroom == null)
            {
                  if (schoolList.isEmpty())
                  {
                        Embed.error(event, "** %s ** has no classes", guild.getName());
                        return;
                  }

                  else if (schoolList.size() == 1)
                  {
                        School school = schoolList.get(0);
                        List<Classroom> classroomList = school.getClassroomList();

                        if (classroomList.size() == 1)
                        {
                              classroom = classroomList.get(0);
                              displayEditMenu(event, classroom);
                        }
                        else
                        {
                              event.sendAsPaginatorWithPageNumbers(classroomList);
                              event.sendMessage("Which class would you like to edit?");
                              jda.addEventListener(new ClassroomEditStateMachine(event, null, classroomList, 2));
                        }
                        return;
                  }

                  event.sendAsPaginatorWithPageNumbers(schoolList);
                  event.sendMessage("Which school has the class you would like to edit");
                  jda.addEventListener(new ClassroomEditStateMachine(event, schoolList, null, 1));
                  return;
            }


            displayEditMenu(event, classroom);
      }

      private void displayEditMenu(CommandEvent event, Classroom classroom)
      {
            var jda = event.getJDA();
            event.sendMessage(UPDATE_MENU, classroom.getName());
            jda.addEventListener(new ClassroomEditStateMachine(event, classroom, 3));
      }

      private static class ClassroomEditStateMachine extends ListenerAdapter implements StateMachine
      {
            private final CommandEvent event;
            private final long authorId;
            private final long channelId;
            private List<School> schoolList;
            private List<Classroom> classroomList;
            private School school;
            private Classroom classroom;
            private int state;

            private String updateColumn;


            public ClassroomEditStateMachine(CommandEvent event, @Nullable List<School> schoolList, @Nullable List<Classroom> classroomList, int state)
            {
                  this.event = event;
                  this.schoolList = schoolList;
                  this.classroomList = classroomList;
                  this.state = state;
                  this.channelId = event.getChannel().getIdLong();
                  this.authorId = event.getUser().getIdLong();
            }


            public ClassroomEditStateMachine(CommandEvent event, School school, int state)
            {
                  this.school = school;
                  this.state = state;
                  this.event = event;
                  this.channelId = event.getChannel().getIdLong();
                  this.authorId = event.getUser().getIdLong();
            }

            public ClassroomEditStateMachine(CommandEvent event, Classroom classroom, int state)
            {
                  this.classroom = classroom;
                  this.state = state;
                  this.event = event;
                  this.channelId = event.getChannel().getIdLong();
                  this.authorId = event.getUser().getIdLong();
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (!Checks.eventMeetsPrerequisites(event, this, channelId, authorId))
                  {
                        return;
                  }

                  String message = event.getMessage().getContentRaw();
                  var jda = event.getJDA();

                  switch (state)
                  {
                        case 1 -> {
                              var success = Processor.validateMessage(event, schoolList);

                              if (success == null)
                              {
                                    return;
                              }


                              school = success;
                              classroomList = school.getClassroomList();

                              Embed.success(event, "** %s ** has successfully been chosen", school.getName());

                              state = Processor.processClassList(this.event, classroomList, UPDATE_MENU);

                        }

                        case 2 -> {
                              var success = Processor.validateMessage(event, classroomList);

                              if (success == null)
                              {
                                    return;
                              }


                              classroom = success;
                              school = classroom.getSchool();

                              Embed.success(event, "** %s ** has successfully been chosen", school.getName());

                              var channel = event.getChannel();

                              channel.sendMessageFormat(UPDATE_MENU, classroom.getName()).queue();

                              state = 3;

                        }

                        case 3 -> state = evaluateMessage(event);

                        case 4 -> state = evaluateChoice(event);

                        case 5 -> {
                              var cmdEvent = this.event;
                              var evaluation = Parser.classTime(cmdEvent.getSchoolbot(), message, classroom);

                              if (!evaluation)
                              {
                                    cmdEvent.sendMessage("Please try again, the time is incorrectly formatted.");
                                    return;
                              }


                              cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, message));
                              Embed.success(event, "Time Successfully changed!");
                              jda.removeEventListener(this);
                        }
                  }
            }

            private int evaluateChoice(GuildMessageReceivedEvent event)
            {
                  String message = event.getMessage().getContentRaw();
                  var jda = event.getJDA();
                  var cmdEvent = this.event;
                  var guild = event.getGuild();
                  String className = classroom.getName().toLowerCase().replaceAll("\\s", "-");


                  switch (updateColumn)
                  {
                        case "name" -> {
                              int length = message.length();

                              if (length >= Constants.MAX_EMBED_TITLE)
                              {
                                    Embed.error(event, "Your class title was %d characters long. It cannot exceed 256 characters", length);
                                    return 4;
                              }

                              String newNames = message.toLowerCase().replace("\\s", "-");


                              var potentialRole = jda.getRoleById(classroom.getRoleID());

                              if (potentialRole != null)
                              {
                                    potentialRole.getManager()
                                            .setName(newNames)
                                            .queue();
                              }

                              var potentialChannel = jda.getTextChannelById(classroom.getChannelID());

                              if (potentialChannel != null)
                              {
                                    potentialChannel.getManager()
                                            .setName(newNames)
                                            .queue();
                              }

                              cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, message));
                              Embed.success(event, "Name successfully changed to: %s", message);
                              // done
                        }

                        case "description" -> {
                              int length = message.length();

                              if (length >= Constants.MAX_FIELD_VALUE)
                              {
                                    Embed.error(event, "Your class description was %d characters long. It cannot exceed 1024 characters", length);
                                    return 4;
                              }


                              Embed.success(event, "Description changed successfully!");
                              cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, message));
                              // done
                        }

                        case "professor_id" -> {
                              List<Professor> professorList = school.getProfessorList();

                              var success = Processor.validateMessage(event, professorList);

                              if (success != null)
                              {
                                    cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, success.getId()));
                                    Embed.success(event, "Professor successfully changed to %s", success.getFullName());
                                    jda.removeEventListener(this);
                              }
                              // done
                        }

                        case "start_date", "end_date" -> {
                              var date = Checks.checkValidDate(message);

                              if (date == null)
                              {
                                    Embed.error(event, "** %s ** is not a valid date. Please try again", message);
                                    return 4;
                              }

                              if (updateColumn.equals("start_date"))
                              {
                                    var classroomEndDate = classroom.getEndDate();

                                    if (!classroomEndDate.minusYears(1).isBefore(date))
                                    {
                                          Embed.error(event, """
                                                           ** %s ** is more than a year away. Most are not a year long.
                                                          `If this is the case please contact: damon#9999`
                                                           """,
                                                  StringUtils.formatDate(date));
                                    }
                                    classroom.setStartDate(Date.valueOf(date));
                              }
                              else
                              {
                                    var classroomStartDate = classroom.getStartDate();

                                    if (!classroomStartDate.plusYears(1).isAfter(date))
                                    {
                                          Embed.error(event, """
                                                   ** %s ** is more than a year away. Most are not a year long.
                                                  `If this is the case please contact: damon#9999`
                                                   """, StringUtils.formatDate(date));
                                          return 4;
                                    }
                                    classroom.setEndDate(Date.valueOf(date));

                              }

                              cmdEvent.sendMessage("""
                                      Please give me the time now.
                                                                            
                                      I will need the day and the time.

                                      ```
                                      Day Mappings:
                                      Mo = Monday
                                      Tu = Tuesday
                                      We = Wednesday
                                      Th = Thursday
                                      Fr = Friday
                                                                            
                                      Format:
                                      Day(s) <start time> - <end time>
                                                                            
                                      Example:
                                      MoWeFri 1:00PM - 1:50PM This class is on Monday Wednesday and Friday from 1:00pm to 1:50pm```
                                      """);
                              return 5;
                        }

                        case "number" -> {
                              var number = Checks.isNum(message);

                              if (number == -1)
                              {
                                    Embed.error(event, "** %s ** is not a number. Please try again!", message);
                                    return 4;
                              }

                              Embed.success(event, "Class Number successfully changed to: %s", number);

                              cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, number));
                        }

                        case "role_id" -> {

                              var msg = event.getMessage();
                              int mentionedRoles = msg.getMentionedRoles().size();


                              if (mentionedRoles != 0)
                              {
                                    var role = msg.getMentionedRoles().get(0);
                                    if (mentionedRoles > 1)
                                    {
                                          Embed.warn(event, """
                                                  You have mentioned ** %d ** roles.
                                                  I will use the first one you mentioned, which is %s
                                                  """, mentionedRoles, role.getAsMention());
                                    }
                                    Embed.success(event, "Role successfully changed to: %s", role.getAsMention());
                                    cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, role.getIdLong()));
                                    break;
                              }


                              if (message.contains("1") || message.toLowerCase().contains("create"))
                              {
                                    var role = guild.createRole()
                                            .setName(className.toLowerCase().replaceAll("\\s", "-"))
                                            .setColor(new Random().nextInt(0xFFFFFF))
                                            .complete();
                                    Embed.success(event, "Role successfully changed to: %s", role.getAsMention());
                                    cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, role.getIdLong()));
                              }
                        }

                        case "channel_id" -> {
                              var msg = event.getMessage();
                              int mentionedChannels = msg.getMentionedChannels().size();


                              if (mentionedChannels != 0)
                              {
                                    var textChannel = msg.getMentionedChannels().get(0);
                                    if (mentionedChannels > 1)
                                    {
                                          Embed.warn(event, """
                                                  You have mentioned ** %d ** channels.
                                                  I will use the first one you mentioned, which is %s
                                                  """, mentionedChannels, textChannel.getAsMention());
                                          Embed.success(event, "Role successfully changed to: %s", textChannel.getAsMention());

                                    }
                                    cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, textChannel.getIdLong()));
                                    break;
                              }


                              if (message.contains("1") || message.toLowerCase().contains("create"))
                              {
                                    var potentialRole = jda.getRoleById(classroom.getRoleID());
                                    TextChannel channel;

                                    if (potentialRole == null)
                                    {
                                          Embed.warn(event, "This class does not have a role assigned to it. When setting up the channel I will only allow admins to view it.");

                                          channel = guild.createTextChannel(className)
                                                  .setName(className)
                                                  .addRolePermissionOverride(guild.getIdLong(), 0L, Permission.ALL_GUILD_PERMISSIONS)
                                                  .complete();
                                    }
                                    else
                                    {
                                          channel = guild.createTextChannel(className)
                                                  .setName(className)
                                                  .addRolePermissionOverride(potentialRole.getIdLong(), Permission.ALL_GUILD_PERMISSIONS, 0L)
                                                  .addRolePermissionOverride(guild.getIdLong(), 0L, Permission.ALL_GUILD_PERMISSIONS)
                                                  .complete();
                                    }
                                    cmdEvent.updateClassroom(new DatabaseDTO(classroom, updateColumn, channel.getIdLong()));
                                    Embed.success(event, "Role successfully changed to: %s", channel.getAsMention());

                              }
                        }

                        case "time" -> {
                              var evaluation = Parser.classTime(cmdEvent.getSchoolbot(), message, classroom);

                              if (!evaluation)
                              {
                                    cmdEvent.sendMessage("Please try again!");
                                    return 4;
                              }

                              Embed.success(event, "Time Successfully changed!");
                        }
                  }
                  jda.removeEventListener(this);
                  return 4;
            }

            private int evaluateMessage(GuildMessageReceivedEvent event)
            {
                  var commandEvent = this.event;
                  var channel = event.getChannel();
                  var jda = event.getJDA();
                  var classroom = this.classroom;
                  String message = event.getMessage().getContentRaw();

                  switch (message.toLowerCase())
                  {
                        case "name", "1" -> {

                              if (classroom.isAutoFilled())
                              {
                                    Embed.error(commandEvent, "You cannot edit the name of a class that was auto filled! I will now stop listening for responses.");
                                    jda.removeEventListener(this);
                                    return 3;
                              }

                              updateColumn = "name";

                              channel.sendMessageFormat("** %s ** is the current class name.. What would you like to change it to?", classroom.getName()).queue();
                        }
                        case "description", "desc", "2" -> {
                              channel.sendMessage("What would you like wto change the class description to").queue();
                              updateColumn = "description";
                        }
                        case "professor", "3" -> {
                              if (classroom.isAutoFilled())
                              {
                                    Embed.error(commandEvent, "You cannot edit the name of a class that was auto filled! I will now stop listening for responses.");
                                    jda.removeEventListener(this);
                                    return 3;
                              }

                              var school = classroom.getSchool();


                              updateColumn = "professor_id";
                        }
                        case "time", "4" -> {

                              commandEvent.sendMessage("""
                                      Please give me the time now.
                                                                            
                                      I will need the day and the time.

                                      ```
                                      Day Mappings:
                                      Mo = Monday
                                      Tu = Tuesday
                                      We = Wednesday
                                      Th = Thursday
                                      Fr = Friday
                                                                            
                                      Format:
                                      Day(s) <start time> - <end time>
                                                                            
                                      Example:
                                      MoWeFri 1:00PM - 1:50PM This class is on Monday Wednesday and Friday from 1:00pm to 1:50pm```
                                      """);

                              updateColumn = "time";
                        }
                        case "start date", "end date", "5", "6" -> {
                              if (classroom.isAutoFilled())
                              {
                                    Embed.error(commandEvent, "You cannot edit the name of a class that was auto filled! I will now stop listening for responses.");
                                    jda.removeEventListener(this);
                                    return 3;
                              }
                              String when = message.split("\\s")[0];
                              channel.sendMessageFormat("""
                                      ** %s ** is the current %s date.
                                                                            
                                      Please start by giving me the date. **Format: M/dd/yyyy**
                                      Examples include: `5/1/2021` and `9/12/2021`
                                      """, when, StringUtils.formatDate(classroom.getStartDate())).queue();
                        }
                        case "number", "7" -> {
                              if (classroom.isAutoFilled())
                              {
                                    Embed.error(commandEvent, "You cannot edit the name of a class that was auto filled! I will now stop listening for responses.");
                                    jda.removeEventListener(this);
                                    return 3;

                              }
                              channel.sendMessageFormat("** %d ** is the class number. What would you like to change it to?", classroom.getNumber()).queue();
                              updateColumn = "number";
                        }
                        case "role", "8" -> {
                              var role = jda.getRoleById(classroom.getRoleID());
                              String messageToSend = role == null ?
                                      """
                                                      This class has no role assigned to it. Here are your options
                                                      ```
                                                      1. Create a Role (You can choose this option by simply saying create or typing 1)
                                                      -or-
                                                      2. Assign one yourself (You can choose this option by simply mentioning the role)```
                                              """
                                      :
                                      String.format("** %s ** is the role assigned to this class what would you like to change it to?", role.getAsMention());

                              channel.sendMessage(messageToSend).queue();
                              updateColumn = "role_id";
                        }
                        case "channel", "9" -> {
                              var textChannel = jda.getTextChannelById(classroom.getChannelID());
                              String messageToSend = textChannel == null ?
                                      """
                                                      This class has no TextChannel assigned to it. Here are your options
                                                      ```
                                                      1. I can create a TextChannel for you (You can choose this option by simply saying create or typing 1)
                                                      -or-
                                                      2. Assign one yourself (You can choose this option by simply mentioning the TextChannel)```
                                              """
                                      :
                                      String.format("** %s ** is the channel assigned to this class what would you like to change it to?", textChannel.getAsMention());

                              channel.sendMessage(messageToSend).queue();
                              updateColumn = "channel_id";
                        }
                        default -> {
                              channel.sendMessageFormat("** %s ** is not a valid choice. Please try again!", message).queue();
                              return 3;
                        }
                  }
                  return 4;
            }
      } // end class


}
