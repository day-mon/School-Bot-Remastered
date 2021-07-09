package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AssignmentEdit extends Command
{
      public AssignmentEdit(Command parent)
      {
            super(parent, "Edits an assignment", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
            setCommandPrerequisites("A valid assignment to edit");
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();
            var schoolList = event.getGuildSchools();


            List<School> schools = new ArrayList<>();
            for (School school : schoolList)
            {
                  List<Classroom> classroomList = new ArrayList<>();

                  for (Classroom classroom : school.getClassroomList())
                  {
                        if (classroom.hasAssignments())
                        {
                              classroomList.add(classroom);

                              // probably bad for runtime but just want to make sure I only get specific schools

                              if (!schools.contains(classroom.getSchool()))
                              {
                                    schools.add(classroom.getSchool());
                              }
                        }
                  }
                  school.setClassroomList(classroomList);
            }


            var genericList = Processor.processGenericList(values, schools, School.class);

            if (genericList == 0)
            {
                  return;
            }

            if (genericList == 1)
            {
                  var classroomList = values.getSchool().getClassroomList()
                          .stream()
                          .filter(Classroom::hasAssignments)
                          .collect(Collectors.toList());

                  values.setClassroomList(classroomList);

                  evaluateClassroom(values);
                  return;
            }

            jda.addEventListener(new AssignmentEditStateMachine(values));


      }


      private void evaluateClassroom(@NotNull StateMachineValues values)
      {
            var event = values.getCommandEvent();
            var jda = event.getJDA();
            var classroomList = values.getClassroomList();

            var processedClassroomList = Processor.processGenericList(values, classroomList, Classroom.class);

            if (processedClassroomList == 1)
            {
                  var assignmentList = values.getAssignmentList();

                  var processedAssignmentList = Processor.processGenericList(values, assignmentList, Assignment.class);

                  if (processedAssignmentList == 1)
                  {
                        var assignment = values.getAssignment();

                        event.sendMessage("** %s ** has been chosen its the only assignment. What would you like to edit?", assignment.getName());
                        event.sendMessage("""
                                ```
                                1. Name
                                2. Description
                                3. Point Amount
                                4. Type
                                5. Due Date
                                6. Due Time```
                                 """);
                  }

                  else if (processedAssignmentList == 2)
                  {
                        values.setState(3);
                  }
            }
            else if (processedClassroomList == 2)
            {
                  values.setState(2);
            }

            jda.addEventListener(new AssignmentEditStateMachine(values));
      }


      private static class AssignmentEditStateMachine extends ListenerAdapter implements StateMachine
      {
            private String updateColumn;
            private final StateMachineValues values;

            public AssignmentEditStateMachine(@NotNull StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);

                  if (!requirementsMet)
                  {
                        return;
                  }

                  String message = event.getMessage().getContentRaw();

                  var channel = event.getChannel();
                  var jda = event.getJDA();
                  var commandEvent = values.getCommandEvent();

                  int state = values.getState();

                  switch (state)
                  {
                        case 1 -> {
                              var success = Processor.validateMessage(values, values.getSchoolList());


                              if (!success)
                              {
                                    return;
                              }

                              var school = values.getSchool();
                              var classroomList = values.getClassroomList();

                              if (classroomList.size() == 1)
                              {
                                    var classroom = values.getClassroom();
                                    EmbedUtils.information(event, """
                                            ** %s ** has been automatically chosen because it is the only class room.
                                                                                        
                                            Which assignment would you like to edit?
                                            """, classroom.getName());
                                    var assignmentList = values.getAssignmentList();
                                    commandEvent.sendAsPaginatorWithPageNumbers(assignmentList);
                                    values.setState(3);
                                    return;
                              }

                              channel.sendMessageFormat("Now that we have selected ** %s, ** I will now need the class you want to the assignment", school.getName()).queue();
                              commandEvent.sendAsPaginatorWithPageNumbers(values.getClassroomList());

                        }

                        case 2 -> {

                              var success = Processor.validateMessage(values, values.getClassroomList());

                              if (!success)
                              {
                                    return;
                              }

                              var classroom = values.getClassroom();
                              var assignmentList = values.getAssignmentList();

                              if (assignmentList.size() == 1)
                              {
                                    var assignment = assignmentList.get(0);
                                    EmbedUtils.information(event, """
                                            ** %s ** has been automatically chosen because it is the only valid assignment.
                                                                                           
                                            Which attribute would you like to edit?
                                                                                           
                                              1. Name
                                              2. Description
                                              3. Point Amount
                                              4. Type
                                              5. Due Date
                                              6. Due Time
                                            """, assignment.getName());
                                    values.setState(4);
                                    return;
                              }
                              channel.sendMessageFormat("Now that we have selected ** %s **, I will now need the assignment you would like to edit", classroom.getName()).queue();
                              commandEvent.sendAsPaginatorWithPageNumbers(values.getAssignmentList());

                        }

                        case 3 -> {

                              var success = Processor.validateMessage(values, values.getAssignmentList());

                              if (!success)
                              {
                                    return;
                              }


                              var assignment = values.getAssignment();

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
                                    EmbedUtils.error(event, "** %s ** is not a valid response.. Try again please", message);
                                    return;
                              }
                              values.incrementMachineState();

                        }

                        case 5 -> {
                              var schoolbot = commandEvent.getSchoolbot();
                              var assignment = values.getAssignment();

                              if (!evaluateUpdate(values))
                              {
                                    return;
                              }
                              commandEvent.sendMessage(assignment.getAsEmbed(schoolbot));
                              jda.removeEventListener(this);
                        }
                  }
            }

            private boolean evaluateUpdate(StateMachineValues values)
            {
                  var event = values.getMessageReceivedEvent();
                  var commandEvent = values.getCommandEvent();
                  var assignment = values.getAssignment();
                  var message = event.getMessage().getContentRaw();

                  switch (updateColumn)
                  {
                        case "name", "description" -> {
                              commandEvent.updateAssignment(new DatabaseDTO(assignment, updateColumn, message));
                              commandEvent.sendMessage(updateColumn.equals("name") ? "Name" : "Description" + " successfully changed to %s", message);

                        }

                        case "points_possible" -> {
                              if (!Checks.isNumber(message))
                              {
                                    EmbedUtils.notANumberError(event, message);
                                    return false;
                              }

                              int newPoints = Integer.parseInt(message);

                              commandEvent.updateAssignment(new DatabaseDTO(assignment, updateColumn, newPoints));
                              commandEvent.sendMessage("Points successfully changed to %d", newPoints);

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
                                    EmbedUtils.error(event, "** %s ** is not a valid entry", message);
                                    return false;
                              }
                              commandEvent.updateAssignment(new DatabaseDTO(assignment, updateColumn, type));
                              commandEvent.sendMessage("Assignment type successfully changed to %s", type.getAssignmentType());
                        }

                        case "due_date" -> {
                              var date = Checks.isValidAssignmentDate(values);

                              if (Objects.isNull(date))
                              {
                                    EmbedUtils.error(event, "** %s ** is not a valid date", message);
                                    return false;
                              }


                              LocalDateTime localDateTime = LocalDateTime.of(date, assignment.getDueDate().toLocalTime());

                              commandEvent.updateAssignment(new DatabaseDTO(assignment, updateColumn, localDateTime));
                              commandEvent.sendMessage("Date successfully changed to %s", localDateTime);

                        }

                        case "due_datet" -> {
                              updateColumn = updateColumn.substring(0, updateColumn.lastIndexOf('t'));

                              LocalDateTime localDateTime;

                              if (!Checks.checkValidTime(message))
                              {
                                    EmbedUtils.error(event, "** %s ** is not a valid time... try again!", message);
                                    return false;
                              }

                              String[] time = message.split(":");


                              if (message.toLowerCase().contains("am"))
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("am", ""));


                                    localDateTime = LocalDateTime.of(assignment.getDueDate().toLocalDate(), LocalTime.of((hour), minute));

                                    if (!localDateTime.isAfter(LocalDateTime.now()))
                                    {
                                          String formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("M/dd/yyyy @ HH:mm"));
                                          EmbedUtils.error(event, "** %s ** is not a valid date.. Try again", formattedTime);
                                          return false;
                                    }

                              }
                              else
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("pm", ""));

                                    if (hour == 12)
                                    {
                                          hour = -12;
                                    }

                                    localDateTime = LocalDateTime.of(assignment.getDueDate().toLocalDate(), LocalTime.of((12 + hour), minute));

                                    if (!localDateTime.isAfter(LocalDateTime.now()))
                                    {
                                          String formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("M/dd/yyyy @ HH:mm"));
                                          EmbedUtils.error(event, "** %s ** is not a valid date.. Try again", formattedTime);
                                          return false;
                                    }

                              }
                              commandEvent.updateAssignment(new DatabaseDTO(assignment, updateColumn, localDateTime));
                              commandEvent.sendMessage("Date successfully changed to %s", localDateTime);

                        }
                  }
                  return true;
            }
      }
}

