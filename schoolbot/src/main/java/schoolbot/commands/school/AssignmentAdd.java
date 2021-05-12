package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.objects.school.Assignment;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.Embed;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentAdd extends Command
{

      /**
       * @param parent
       */
      public AssignmentAdd(Command parent)
      {
            super(parent, " ", " ", 0);
            addFlags(CommandFlag.DATABASE);

      }

      /**
       * @param event Arguments sent to the command.
       *              <p>
       *              TODO: Start this command by 05/02/2021
       *              <p>
       *              Potential Issues;
       *              - A user could have multiple school roles.
       */
      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> school.getClassesSize() > 0)
                    .collect(Collectors.toList());

            Classroom classroom = null;
            int stateToGoto = 1;

            Embed.information(event, "Schools with classes assigned to it will be listed..");

            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR))
            {
                  event.sendMessage("To add an assignment you can either start with your **class number** or the **class name** ");
            }


            if (schools.isEmpty())
            {
                  Embed.error(event, "This server does not have any school associated with it!");
                  event.getJDA().removeEventListener(this);
                  return;
            }
            else if (schools.size() == 1)
            {
                  classroom = new Classroom();
                  classroom.setSchool(schools.get(0));
                  event.getChannel().sendMessageFormat("** %s ** has been selected because there is only one school in this server", classroom.getSchool().getSchoolName()).queue();
                  event.getChannel().sendMessage("Would you like to continue?").queue();
                  stateToGoto = 2;
            }
            else
            {
                  event.sendMessage("Please choose the School ID of the school you want to add the assignment to ");
                  event.getAsPaginatorWithPageNumbers(schools);
            }


            event.getJDA().addEventListener(new AssignmentAddStateMachine(event, schools, classroom, stateToGoto));

      }

      public static class AssignmentAddStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private int state;
            private final CommandEvent commandEvent;
            private Schoolbot schoolbot;
            private final Assignment assignment;
            private Classroom classroom;
            private List<Classroom> classroomList;
            private final List<School> schools;
            private LocalDate date;


            public AssignmentAddStateMachine(CommandEvent event, List<School> schools, Classroom classroom, int state)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.schoolbot = event.getSchoolbot();
                  this.commandEvent = event;
                  this.schools = schools;
                  this.classroom = classroom == null ? new Classroom() : classroom;
                  this.assignment = new Assignment();
                  this.state = state;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().isBot()) return;
                  if (event.getChannel().getIdLong() != channelID) return;
                  if (event.getAuthor().getIdLong() != authorID) return;

                  JDA jda = event.getJDA();
                  MessageChannel channel = event.getChannel();
                  String content = event.getMessage().getContentRaw();
                  Member user = event.getMember();
                  Guild guild = event.getGuild();

                  if (content.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Okay aborting..").queue();
                        jda.removeEventListener(this);
                        return;
                  }


                  switch (state)
                  {

                        case 1 -> {
                              if (!Checks.isNumber(content))
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", content);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(content);

                              if (!Checks.between(pageNumber, 1, schools.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the school ids...", content);
                                    return;
                              }

                              classroom.setSchool(schools.get(pageNumber - 1));
                              Embed.success(event, "** %s ** successfully selected", classroom.getSchool().getSchoolName());
                              channel.sendMessage("Would you like to continue?").queue();
                              state = 2;
                        }

                        case 2 -> {
                              if (!content.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay goodbye").queue();
                                    jda.removeEventListener(this);
                                    return;
                              }

                              if (user.hasPermission(Permission.ADMINISTRATOR))
                              {
                                    classroomList = commandEvent.getSchool(commandEvent, classroom.getSchool().getSchoolName()).getClassroomList();

                                    if (classroomList.isEmpty())
                                    {
                                          Embed.error(event, "** %s ** does not have any classes associated with it", guild.getName());
                                          jda.removeEventListener(this);
                                    }
                                    else if (classroomList.size() == 1)
                                    {
                                          Embed.success(event, "** %s ** has been selected automatically because you only have one class associated with you!", classroomList.get(0).getClassName());
                                          channel.sendMessageFormat("""
                                                  Now that we have all that sorted the fun stuff can start %s
                                                  Im going to start by asking for your assignment name
                                                  """, Emoji.SMILEY_FACE.getAsChat()
                                          ).queue();
                                          assignment.setClassroom(classroomList.get(0));
                                          state = 4;
                                    }
                                    else
                                    {
                                          channel.sendMessage("Choose a page number corresponding with the school").queue();
                                          commandEvent.getAsPaginatorWithPageNumbers(classroomList);

                                          state = 3;
                                    }
                              }
                              // first check all users roles...
                              else
                              {
                                    List<Long> roleIDs = user.getRoles().stream()
                                            .map(Role::getIdLong)
                                            .collect(Collectors.toList());
                                    List<Classroom> classes = commandEvent.getSchool(commandEvent, classroom.getSchool().getSchoolName()).getClassroomList();


                                    // This is actually horrifically slow and not really scalable could use a hashmap or store these before hand .
                                    for (Classroom classroom : classes)
                                    {
                                          if (roleIDs.contains(classroom.getRoleID()))
                                          {
                                                classes.add(classroom);
                                          }
                                    }

                                    if (classes.isEmpty())
                                    {
                                          Embed.error(event, "You do not have any roles associated with any classes");
                                          jda.removeEventListener(this);
                                    }
                                    else if (classes.size() == 1)
                                    {
                                          Embed.success(event, "** %s ** has been selected automatically because you only have one class associated with you!", classes.get(0).getClassName());
                                          channel.sendMessageFormat("""
                                                  Now that we have all that sorted the fun stuff can start %s
                                                  Im going to start by asking for your assignment name
                                                  """, Emoji.SMILEY_FACE.getAsChat()
                                          ).queue();
                                          assignment.setClassroom(classroomList.get(0));
                                          state = 4;
                                    }
                                    else
                                    {
                                          channel.sendMessage("You have more than one class associated with you.. Please give me the page number").queue();
                                          commandEvent.getAsPaginatorWithPageNumbers(classroomList);
                                          state = 3;
                                    }
                              }
                        }
                        case 3 -> {
                              if (!Checks.isNumber(content))
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", content);
                                    return;
                              }

                              int index = Integer.parseInt(content) - 1;

                              if (!Checks.between(index + 1, 1, classroomList.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the class ids...", content);
                                    event.getJDA().removeEventListener(this);
                                    return;
                              }
                              assignment.setClassroom(classroomList.get(index));
                              Embed.success(event, "** %s ** has successfully been selected", this.classroom.getClassName());
                              channel.sendMessageFormat("""
                                      Now that we have all that sorted the fun stuff can start %s
                                      Im going to start by asking for your assignment name
                                      """, Emoji.SMILEY_FACE.getAsChat()
                              ).queue();
                              state = 4;
                        }

                        case 4 -> {
                              assignment.setProfessorID(assignment.getClassroom().getProfessorID());
                              assignment.setName(content);

                              Embed.success(event, "** %s ** has successfully been added as Assignment name..", assignment.getName());
                              channel.sendMessageFormat("Please give me a small description about the assignment. You can change it later so if you wanna speed through this its fine %s", Emoji.SMILEY_FACE.getAsChat()).queue();
                              state = 5;
                        }

                        case 5 -> {
                              assignment.setDescription(content);
                              Embed.success(event, "Description has successfully been added as Assignment name..");
                              channel.sendMessage("Okay got it im going to need the point amount for the assignment.. If you don't know just put 'idk' or 0").queue();
                              state = 6;
                        }

                        case 6 -> {
                              if (!Checks.isNumber(content) || content.toLowerCase().contains("idk"))
                              {
                                    Embed.error(event, "** %s ** is not a number.. try again!", content);
                                    state = 6;
                                    return;
                              }

                              int points = content.toLowerCase().contains("idk") ? 0 : Integer.parseInt(content);

                              assignment.setPoints(points);
                              Embed.success(event, "** %d ** has been set as ** %s ** point amount", points, assignment.getName());
                              channel.sendMessage("""
                                      Now I will need the type of assignment it is 
                                      ```
                                      Valid Answers
                                      1. Exam
                                      2. Quiz
                                      3. Extra Credit
                                      4. Homework
                                      ```
                                      """).queue();
                              state = 7;
                        }

                        case 7 -> {
                              String message = content.toLowerCase();

                              if (message.contains("exam") || message.contains("1"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.EXAM);
                              }
                              else if (message.contains("homework") || message.contains("work") || message.contains("4"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.HOMEWORK);
                              }
                              else if (message.contains("quiz") || message.contains("2"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.QUIZ);
                              }
                              else if (message.contains("extra") || message.contains("credit") || message.contains("3"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.EXTRA_CREDIT);
                              }
                              else
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", content);
                                    return;
                              }
                              Embed.success(event, "** %s ** has been set as your assignment type", assignment.getAssignmentType().getAssignmentType());
                              channel.sendMessage("""
                                      I will need your due date..
                                      Please use the following format: `M/dd/yyy`
                                      An Example: `2/9/2004`
                                      """).queue();
                              state = 8;
                        }

                        case 8 -> {
                              if (!Checks.isValidAssignmentDate(content, classroom))
                              {
                                    Embed.error(event, "** %s ** is not a valid date.. Please try again", content);
                                    return;
                              }

                              date = LocalDate.parse(content, DateTimeFormatter.ofPattern("M/d/yyyy"));

                              Embed.success(event, "** %s ** successfully set as this assignments due date", date.toString());
                              channel.sendMessage("""
                                      Lastly I will need the time in which your assignment is due
                                      Please use the following format: `HH:mm AM/PM`
                                      An Example: `12:30pm`
                                      """).queue();
                              state = 9;
                        }

                        case 9 -> {
                              if (!Checks.checkValidTime(content))
                              {
                                    Embed.error(event, "** %s ** is not a valid time... try again!", content);
                                    return;
                              }

                              String[] time = content.split(":");


                              if (content.toLowerCase().contains("am"))
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("am", ""));

                                    assignment.setDueDate(OffsetDateTime.of(date, LocalTime.of(hour, minute), ZoneOffset.UTC));

                              }
                              else
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("pm", ""));

                                    if (hour == 12)
                                    {
                                          Embed.error(event, "That doesnt make sense.... Please input a valid date");
                                          return;
                                    }

                                    assignment.setDueDate(OffsetDateTime.of(date, LocalTime.of((12 + hour), minute), ZoneOffset.UTC));
                              }

                              commandEvent.addAssignment(commandEvent, assignment);

                              Embed.success(event, "** %s ** has successfully been added to ** %s **", assignment.getName(), assignment.getClassroom().getClassName());
                              event.getJDA().removeEventListener(this);

                        }


                  }

            }
      }
}


