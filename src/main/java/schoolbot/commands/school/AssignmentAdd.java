package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentAdd extends Command
{
      public AssignmentAdd(Command parent)
      {
            super(parent, "Adds an assignment to the target class", "[none]", 0);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            Classroom classroom = Checks.messageSentFromClassChannel(event);

            int stateToGoto = 1;

            if (classroom != null)
            {
                  event.sendMessage("""
                          ** %s ** has been selected because it you sent it from this channel
                          Please give me the name of the assignment!
                          """, classroom.getClassName());
                  stateToGoto = 4;
            }
            else
            {
                  if (member.hasPermission(Permission.ADMINISTRATOR))
                  {
                        if (schools.isEmpty())
                        {
                              Embed.error(event, "This server does not have any school associated with it");
                        }
                        else if (schools.size() == 1)
                        {
                              classroom = new Classroom();
                              classroom.setSchool(schools.get(0));
                              channel.sendMessageFormat("** %s ** has been selected because there is only one school in this server", classroom.getSchool().getSchoolName()).queue();
                              event.sendMessage("Would you like to continue?");
                        }
                        else
                        {
                              event.sendMessage("Please choose the School ID of the school you want to add the assignment to ");
                              event.getAsPaginatorWithPageNumbers(schools);
                        }
                  }
                  else
                  {
                        // Not an administrator

                        List<Long> validRoles = Checks.validRoleCheck(event);

                        List<Classroom> classrooms = event.getGuildClasses()
                                .stream()
                                .filter(classes -> Collections.frequency(validRoles, classes.getRoleID()) > 1)
                                .filter(classes -> !classes.getAssignments().isEmpty())
                                .collect(Collectors.toList());

                        if (classrooms.isEmpty())
                        {
                              Embed.error(event, "You do nto have any roles that indicate you attend any classes");
                        }
                        else if (classrooms.size() == 1)
                        {
                              classroom = classrooms.get(0);
                              Embed.success(event, "** %s ** has been selected because it is the only role you have", classroom.getClassName());
                        }
                        else
                        {
                              event.getAsPaginatorWithPageNumbers(classrooms);
                              event.sendMessage("Select a class by page number (%d / %d)", 1, classrooms.size());
                        }
                  }
            }
            event.getJDA().addEventListener(new AssignmentAddStateMachine(event, schools, classroom, stateToGoto));
      }


      public static class AssignmentAddStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private int state;
            private final CommandEvent commandEvent;
            private final Assignment assignment;
            private Classroom classroom;
            private List<Classroom> classroomList;
            private final List<School> schools;
            private LocalDate date;


            public AssignmentAddStateMachine(CommandEvent event, List<School> schools, Classroom classroom, int state)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.commandEvent = event;
                  this.schools = schools;
                  this.classroom = classroom == null ? new Classroom() : classroom;
                  this.assignment = new Assignment();
                  this.state = state;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().getIdLong() != authorID) return;
                  if (event.getChannel().getIdLong() != channelID) return;

                  MessageChannel channel = event.getChannel();
                  String message = event.getMessage().getContentRaw();
                  JDA jda = event.getJDA();
                  Guild guild = event.getGuild();


                  if (message.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Aborting...").queue();
                        jda.removeEventListener(this);
                        return;
                  }

                  switch (state)
                  {
                        case 1 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, """
                                            ** %s ** is not a number
                                            Please Enter a number
                                            """, message);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(message);

                              if (!Checks.between(pageNumber, 1, schools.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the school ids...", message);
                                    return;
                              }

                              classroom.setSchool(schools.get(pageNumber - 1));
                              Embed.success(event, "** %s ** successfully selected", classroom.getSchool().getSchoolName());
                              channel.sendMessage("Would you like to continue?").queue();
                              state = 2;
                        }


                        case 2 -> {
                              if (!message.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay goodbye").queue();
                                    jda.removeEventListener(this);
                                    return;
                              }

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
                                    this.classroom = classroomList.get(0);
                                    state = 4;
                              }
                        }

                        case 3 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", message);
                                    return;
                              }

                              int index = Integer.parseInt(message) - 1;

                              if (!Checks.between(index + 1, 1, classroomList.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the class ids...", message);
                                    event.getJDA().removeEventListener(this);
                                    return;
                              }
                              this.classroom = classroomList.get(index);
                              Embed.success(event, "** %s ** has successfully been selected", this.classroom.getClassName());
                              channel.sendMessageFormat("""
                                              Now that we have all that sorted the fun stuff can start %s
                                              Im going to start by asking for your assignment name
                                              """,
                                      Emoji.SMILEY_FACE.getAsChat()).queue();
                              state = 4;
                        }

                        case 4 -> {
                              assignment.setProfessorID(this.classroom.getProfessorID());
                              assignment.setClassroom(this.classroom);
                              // ??? what am i doing..
                              assignment.setName(message);

                              Embed.success(event, "** %s ** has successfully been added as Assignment name..", assignment.getName());
                              channel.sendMessageFormat("Please give me a small description about the assignment. You can change it later so if you wanna speed through this its fine %s", Emoji.SMILEY_FACE.getAsChat()).queue();
                              state = 5;
                        }

                        case 5 -> {
                              assignment.setDescription(message);
                              Embed.success(event, "Description has successfully been added as Assignment name..");
                              channel.sendMessage("Okay got it im going to need the point amount for the assignment.. If you don't know just put 'idk' or 0").queue();
                              state = 6;
                        }

                        case 6 -> {
                              if (!Checks.isNumber(message) || message.toLowerCase().contains("idk"))
                              {
                                    Embed.error(event, "** %s ** is not a number.. try again!", message);
                                    state = 6;
                                    return;
                              }

                              int points = message.toLowerCase().contains("idk") ? 0 : Integer.parseInt(message);

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
                              String content = message.toLowerCase();

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
                                    Embed.error(event, "** %s ** is not a valid entry", message);
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
                              if (!Checks.isValidAssignmentDate(message, classroom))
                              {
                                    Embed.error(event, "** %s ** is not a valid date.. Please try again", message);
                                    return;
                              }

                              date = LocalDate.parse(message, DateTimeFormatter.ofPattern("M/d/yyyy"));

                              Embed.success(event, "** %s ** successfully set as this assignments due date", date.toString());
                              channel.sendMessage("""
                                      Lastly I will need the time in which your assignment is due
                                      Please use the following format: `HH:mm AM/PM`
                                      An Example: `12:30pm`
                                      """).queue();
                              state = 9;
                        }

                        case 9 -> {
                              if (!Checks.checkValidTime(message))
                              {
                                    Embed.error(event, "** %s ** is not a valid time... try again!", message);
                                    return;
                              }

                              String[] time = message.split(":");


                              if (message.toLowerCase().contains("am"))
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("am", ""));


                                    assignment.setDueDate(LocalDateTime.of(date, LocalTime.of(hour, minute)));
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

                                    assignment.setDueDate(LocalDateTime.of(date, LocalTime.of((12 + hour), minute)));
                              }

                              commandEvent.addAssignment(commandEvent, assignment);

                              Embed.success(event, "** %s ** has successfully been added to ** %s **", assignment.getName(), assignment.getClassroom().getClassName());
                              event.getJDA().removeEventListener(this);

                        }
                  }
            }
      }

}
