package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
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
import schoolbot.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static schoolbot.commands.school.AssignmentEdit.AssignmentEditStateMachine.sendMenu;

public class AssignmentEdit extends Command
{
      public AssignmentEdit(Command parent)
      {
            super(parent, "Edits an assignment", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
            addCommandPrerequisites("A valid assignment to edit");
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
                        values.setState(4);
                        sendMenu(String.format("%s is the only assignment. What would you like to edit", values.getAssignment().getName()), values);
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


      static class AssignmentEditStateMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;
            private static final boolean selectionMenuOccurred = false;

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
                                    sendMenu(String.format("** %s ** has been automatically chosen because it is the only valid assignment. What would you like to edit", values.getAssignment().getName()), values);
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
                              sendMenu(String.format("What attribute of %s would you like to edit", assignment.getName()), values);

                        }

                        case 4 -> {
                              var schoolbot = commandEvent.getSchoolbot();
                              var assignment = values.getAssignment();

                              if (!evaluateUpdate(values))
                              {
                                    return;
                              }
                              channel.sendMessageEmbeds(assignment.getAsEmbed(schoolbot))
                                      .append("Assignment successfully edited!")
                                      .queue();
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
                  var updateColumn = values.getUpdateColumn();

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

                        case "due_date" -> {
                              var date = Checks.isValidAssignmentDate(values);

                              if (Objects.isNull(date))
                              {
                                    EmbedUtils.error(event, "** %s ** is not a valid date", message);
                                    return false;
                              }


                              LocalDateTime localDateTime = LocalDateTime.of(date, assignment.getDueDate().toLocalTime());

                              commandEvent.updateAssignment(new DatabaseDTO(assignment, updateColumn, localDateTime));
                              commandEvent.sendMessage("Date successfully changed to %s", StringUtils.formatDate(localDateTime));

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


            public static void sendMenu(String placeHolder, StateMachineValues values)
            {
                  var assignment = values.getAssignmentList().get(0);
                  var commandEvent = values.getCommandEvent();
                  var channel = values.getCommandEvent().getChannel();

                  List<SelectOption> selectOptionList = List.of(
                          SelectOption.of("Name", "name"),
                          SelectOption.of("Description", "description"),
                          SelectOption.of("Point Amount", "pointAmount"),
                          SelectOption.of("Type", "type"),
                          SelectOption.of("Due Date", "dueDate"),
                          SelectOption.of("Due Time", "dueTime")
                  );

                  commandEvent.sendMenuAndAwait(placeHolder, selectOptionList,
                          (selectionMenuEvent) ->
                          {
                                var elementChosen = selectionMenuEvent.getValues().get(0);

                                switch (elementChosen)
                                {
                                      case "name" -> {
                                            values.setUpdateColumn("name");
                                            channel.sendMessageFormat("Please give me an updated name of the assignment").queue();
                                      }

                                      case "description" -> {
                                            values.setUpdateColumn("description");
                                            channel.sendMessageFormat("Please give me an updated description of the assignment").queue();
                                      }

                                      case "pointAmount" -> {
                                            values.setUpdateColumn("points_possible");
                                            channel.sendMessageFormat("Please give me an updated point amount of the assignment").queue();
                                      }

                                      case "type" -> {
                                            if (values.getMachine() != null)
                                                  values.getJda().removeEventListener(values.getMachine());

                                            values.setUpdateColumn("type");
                                            List<SelectOption> selectOptions = List.of(
                                                    SelectOption.of("Exam", "exam"),
                                                    SelectOption.of("Quiz", "quiz"),
                                                    SelectOption.of("Extra Credit", "extra_credit"),
                                                    SelectOption.of("Homework", "homework"),
                                                    SelectOption.of("Paper", "paper")
                                            );


                                            commandEvent.sendMenuAndAwait("Please choose your assignment type", selectOptions, (selectionMenuEvent1) ->
                                            {
                                                  var elementChosen1 = selectionMenuEvent1.getValues().get(0);


                                                  switch (elementChosen1)
                                                  {
                                                        case "exam" -> values.getAssignment().setType(Assignment.AssignmentType.EXAM);
                                                        case "extra_credit" -> values.getAssignment().setType(Assignment.AssignmentType.EXTRA_CREDIT);
                                                        case "homework" -> values.getAssignment().setType(Assignment.AssignmentType.HOMEWORK);
                                                        case "quiz" -> values.getAssignment().setType(Assignment.AssignmentType.QUIZ);
                                                        case "paper" -> values.getAssignment().setType(Assignment.AssignmentType.PAPER);
                                                  }

                                                  var assignmentType = values.getAssignment().getType();
                                                  commandEvent.updateAssignment(new DatabaseDTO(assignment, values.getUpdateColumn(), assignmentType));
                                                  channel.sendMessageEmbeds(assignment.getAsEmbed(commandEvent.getSchoolbot()))
                                                          .appendFormat("**%s** type has successfully been changed to %s", assignment.getName(), assignmentType.getAssignmentType())
                                                          .queue();
                                            });

                                      }

                                      case "dueDate" -> {
                                            values.setUpdateColumn("due_date");
                                            channel.sendMessage("""
                                                    Please give me the updated due date
                                                    Please use the following format: `M/dd/yyyy`
                                                    An Example: `2/9/2004`
                                                    """).queue();
                                      }

                                      case "dueTime" -> {
                                            values.setUpdateColumn("due_datet");
                                            channel.sendMessage("""
                                                    Please give me the updated due time
                                                    Please use the following format: `HH:mm AM/PM`
                                                    An Example: `12:30pm` or `8:30am`
                                                    """).queue();
                                      }
                                }
                                values.setState(4);
                          });
            }
      }
}

