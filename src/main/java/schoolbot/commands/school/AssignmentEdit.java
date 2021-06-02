package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentEdit extends Command
{
      public AssignmentEdit(Command parent)
      {
            super(parent, "Edits an assignment", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            JDA jda = event.getJDA();
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> school.getClassroomList()
                            .stream()
                            .anyMatch(Classroom::hasAssignments))
                    .collect(Collectors.toList());

            if (schools.isEmpty())
            {
                  Embed.error(event, "** %s ** has no classes with assignments in them", event.getGuild().getName());
                  return;
            }

            if (schools.size() == 1)
            {
                  School school = schools.get(0);

                  evaluateClassroom(event, school.getClassroomList());
                  return;
            }

            if (schools.size() > 1)
            {
                  event.getAsPaginatorWithPageNumbers(schools);
                  event.sendMessage("What school would you like to edit an assignment in...");
                  jda.addEventListener(new AssignmentEditStateMachine(event, schools, null, null, 1));
                  return;
            }

            School school = schools.get(0);

            evaluateClassroom(event, school.getClassroomList());
      }


      private void evaluateClassroom(CommandEvent event, List<Classroom> classroomList)
      {
            JDA jda = event.getJDA();
            if (classroomList.size() == 1)
            {
                  Classroom classroom = classroomList.get(0);
                  List<Assignment> assignments = classroom.getAssignments();


                  if (assignments.size() == 1)
                  {
                        Assignment assignment = classroom.getAssignments().get(0);
                        event.sendMessage("** %s ** has been chosen its the only assignment..", assignment.getName());
                        jda.addEventListener(new AssignmentEditStateMachine(event, assignment));
                  }
                  else
                  {
                        event.getAsPaginatorWithPageNumbers(assignments);
                        event.sendMessage("Please give me the page number of the assignment you want to edit");
                        jda.addEventListener(new AssignmentEditStateMachine(event, null, classroomList, assignments, 0));

                  }
            }

            if (classroomList.size() > 1)
            {
                  event.getAsPaginatorWithPageNumbers(classroomList);
                  event.sendMessage("Please give me the page number of the class that contains the assignment you want to edit");
                  jda.addEventListener(new AssignmentEditStateMachine(event, null, classroomList, null, 2));
            }
      }


      public class AssignmentEditStateMachine extends ListenerAdapter
      {

            private final CommandEvent commandEvent;
            private final long authorID, channelID;
            private List<School> schools;
            private List<Classroom> classroomList;
            private List<Assignment> assignmentList;
            private School school;
            private Classroom classroom;
            private Assignment assignment;
            private int state;
            private String updateColumn;

            public AssignmentEditStateMachine(CommandEvent event, Assignment assignment)
            {
                  this.commandEvent = event;
                  this.authorID = event.getUser().getIdLong();
                  this.channelID = event.getChannel().getIdLong();
                  this.assignment = assignment;
            }

            public AssignmentEditStateMachine(@NotNull CommandEvent event, @Nullable List<School> schoolList,
                                              @Nullable List<Classroom> classroomList, @Nullable List<Assignment> assignments,
                                              int state)
            {
                  this.commandEvent = event;
                  this.authorID = event.getUser().getIdLong();
                  this.channelID = event.getChannel().getIdLong();
                  this.classroomList = classroomList;
                  this.schools = schoolList;
                  this.assignmentList = assignments;
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

                              if (!Checks.between(pageNumber, schools.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the school ids...", message);
                                    return;
                              }

                              school = schools.get(pageNumber - 1);
                              classroomList = school.getClassroomList();
                              channel.sendMessageFormat("Now that we have selected ** %s **, I will now need the class you want to the assignment", school.getName()).queue();
                              commandEvent.getAsPaginatorWithPageNumbers(school.getClassroomList());
                              state = 2;
                        }

                        case 2 -> {
                              int classSize = classroomList.size();
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, """
                                            ** %s ** is not a number
                                            Please Enter a number
                                            """, message);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(message);

                              if (!Checks.between(pageNumber, classSize))
                              {
                                    Embed.error(event, "** %s ** was not one of the school ids...", message);
                                    return;
                              }

                              classroom = classroomList.get(pageNumber - 1);
                              this.assignmentList = classroom.getAssignments();
                              channel.sendMessageFormat("Now that we have selected ** %s **, I will now need the assignment you would like to edit", classroom.getName()).queue();
                              commandEvent.getAsPaginatorWithPageNumbers(assignmentList);
                              state = 3;
                        }

                        case 3 -> {
                              int assignmentSize = assignmentList.size();
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, """
                                            ** %s ** is not a number
                                            Please Enter a number
                                            """, message);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(message);

                              if (!Checks.between(pageNumber, assignmentSize))
                              {
                                    Embed.error(event, "** %s ** was not one of the page numbers.", message);
                                    return;
                              }

                              assignment = assignmentList.get(pageNumber - 1);
                              channel.sendMessageFormat("What attribute of ** %s ** would you like to edit", assignment.getName()).queue();
                              channel.sendMessage("""
                                      ```
                                         1. Name
                                         2. Description
                                         3. Point Amount
                                         4. Type
                                         5. Due Date
                                         6. Due Time```
                                       """).queue();
                              state = 4;
                        }

                        case 4 -> {
                              String content = message.toLowerCase();

                              if (content.equals("name") || content.equals("1"))
                              {
                                    updateColumn = "name";
                                    channel.sendMessageFormat("Please give me an updated name of the assignment").queue();

                              }
                              else if (content.equals("description") || content.equals("2"))
                              {
                                    updateColumn = "description";
                                    channel.sendMessageFormat("Please give me an updated description of the assignment").queue();
                              }
                              else if (content.contains("point") || content.contains("amount") || content.equals("3"))
                              {
                                    updateColumn = "points_possible";
                                    channel.sendMessageFormat("Please give me an updated point amount of the assignment").queue();

                              }
                              else if (content.contains("4") || content.contains("type"))
                              {
                                    updateColumn = "type";
                                    channel.sendMessage("""
                                            Now I will need the type of assignment it is
                                            ```
                                            Valid Answers
                                            1. Exam
                                            2. Quiz
                                            3. Extra Credit
                                            4. Homework
                                            5. Paper
                                            ```
                                            """).queue();
                              }
                              else if (content.equals("5") || content.contains("date"))
                              {
                                    updateColumn = "due_date";
                                    channel.sendMessage("""
                                            Please give me the updated due date
                                            Please use the following format: `M/dd/yyyy`
                                            An Example: `2/9/2004`
                                            """).queue();
                              }
                              else if (content.equals("6") || content.contains("time"))
                              {
                                    updateColumn = "due_datet";
                                    channel.sendMessage("""
                                            Please give me the updated due time
                                            Please use the following format: `HH:mm AM/PM`
                                            An Example: `12:30pm` or `8:30am`
                                            """).queue();
                              }
                              else
                              {
                                    Embed.error(event, "** %s ** is not a valid response.. Try again please", message);
                                    return;
                              }
                              state = 5;

                        }

                        case 5 -> {
                              evaluateUpdate(commandEvent, message);
                        }
                  }
            }

            private void evaluateUpdate(CommandEvent event, String message)
            {
                  JDA jda = event.getJDA();

                  switch (updateColumn)
                  {
                        case "name", "description" -> {
                              commandEvent.updateAssignment(commandEvent, new DatabaseDTO(assignment, updateColumn, message));
                              event.sendMessage(message.equals("name") ? "Name" : "Description" + " successfully changed to %s", message);

                        }

                        case "points_possible" -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.notANumberError(event.getEvent(), message);
                              }

                              int newPoints = Integer.parseInt(message);

                              commandEvent.updateAssignment(event, new DatabaseDTO(assignment, updateColumn, newPoints));
                              event.sendMessage("Points successfully changed to %d", newPoints);

                        }

                        case "type" -> {
                              Assignment.AssignmentType type;
                              if (message.contains("exam") || message.contains("1"))
                              {
                                    type = Assignment.AssignmentType.EXAM;
                              }
                              else if (message.contains("paper") || message.contains("5"))
                              {
                                    type = Assignment.AssignmentType.PAPER;
                              }
                              else if (message.contains("homework") || message.contains("work") || message.contains("4"))
                              {
                                    type = Assignment.AssignmentType.HOMEWORK;
                              }
                              else if (message.contains("quiz") || message.contains("2"))
                              {
                                    type = Assignment.AssignmentType.QUIZ;
                              }
                              else if (message.contains("extra") || message.contains("credit") || message.contains("3"))
                              {
                                    type = Assignment.AssignmentType.EXTRA_CREDIT;
                              }
                              else
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", message);
                                    return;
                              }
                              commandEvent.updateAssignment(commandEvent, new DatabaseDTO(assignment, updateColumn, type));
                              event.sendMessage("Assignment type successfully changed to %s", type.getAssignmentType());
                        }

                        case "due_date" -> {
                              if (!Checks.isValidAssignmentDate(message, assignment.getClassroom()))
                              {
                                    Embed.error(event, "** %s ** is not a valid date", message);
                              }

                              LocalDateTime localDateTime = LocalDateTime.of(LocalDate.parse(message, DateTimeFormatter.ofPattern("M/d/yyyy")), assignment.getDueDate().toLocalTime());

                              commandEvent.updateAssignment(event, new DatabaseDTO(assignment, updateColumn, localDateTime));
                              event.sendMessage("Date successfully changed to %s", localDateTime);

                        }


                        // TODO: Add some date checks and remove old reminders n such
                        case "due_datet" -> {
                              updateColumn = updateColumn.substring(0, updateColumn.lastIndexOf("t"));

                              LocalDateTime localDateTime;

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


                                    localDateTime = LocalDateTime.of(assignment.getDueDate().toLocalDate(), LocalTime.of((hour), minute));
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

                                    localDateTime = LocalDateTime.of(assignment.getDueDate().toLocalDate(), LocalTime.of((12 + hour), minute));
                              }
                              commandEvent.updateAssignment(event, new DatabaseDTO(assignment, updateColumn, localDateTime));
                              event.sendMessage("Date successfully changed to %s", localDateTime);

                        }
                  }
                  jda.removeEventListener(this);
            }
      }
}

