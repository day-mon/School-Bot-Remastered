package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.misc.StateMachine;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;
import schoolbot.util.Processor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AssignmentAdd extends Command
{
      public AssignmentAdd(Command parent)
      {
            super(parent, "Adds an assignment to the target class", "[none]", 0);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }

      private static void processClassroomList(StateMachineValues values)
      {

            var commandEvent = values.getCommandEvent();
            var classroomList = values.getClassroomList();

            var success = Processor.processGenericList(values, classroomList, Classroom.class);


            if (success == 1)
            {
                  var classroom1 = classroomList.get(0);

                  Embed.information(commandEvent, """
                          ** %s ** has been selected because it is the only role I can recognize.
                                                                
                          I would like to proceed with the assignment name you would like.
                                                                
                          If you would like to exit at any time (including now) please type '**exit**' or '**stop**'
                                                                
                          """, classroom1.getName());

                  values.setState(4);
            }
            else
            {
                  values.setState(3);
            }
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();
            var channel = event.getChannel();

            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getClassroomList().isEmpty())
                    .collect(Collectors.toList());

            Classroom classroom = Checks.messageSentFromClassChannel(values);

            if (classroom == null)
            {
                  var member = event.getMember();
                  var admin = member.hasPermission(Permission.ADMINISTRATOR);

                  if (admin)
                  {
                        var processedElement = Processor.processGenericList(values, schools, School.class);

                        if (processedElement == 0)
                        {
                              return;
                        }

                        if (processedElement == 1)
                        {
                              var school = values.getSchool();
                              var classroomList = school.getClassroomList();

                              event.sendSelfDeletingMessage("** %s ** has been selected. **This message will be deleted in 10 seconds to reduce clutter!**");

                              processClassroomList(values);
                        }
                  }
                  else
                  {
                        // Not an admin

                        var classroomList = Processor.processUserRoles(values);

                        if (classroomList.isEmpty())
                        {
                              Embed.error(event, "You have no association to any classes, and you did not send this message in any channels associated with a class");
                              return;
                        }

                        processClassroomList(values);
                  }
            }


            // Redundant to repeat but youd have to do a lot of scrolling to remember why you are here
            if (classroom != null)
            {
                  Embed.information(event, """
                          ** %s ** has been selected because it is the only class I can recognize.
                                                                
                          I would like to proceed with the assignment name you would like.
                                                                
                          If you would like to exit at any time (including now) please type '**exit**' or '**stop**'
                                                                
                          """, classroom.getName());
                  values.setState(4);
            }


            jda.addEventListener(new AssignmentAddStateMachine(values));
      }

      public static class AssignmentAddStateMachine extends ListenerAdapter implements StateMachine
      {
            private LocalDate date;
            private final StateMachineValues values;

            public AssignmentAddStateMachine(StateMachineValues values)
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
                  var guild = event.getGuild();

                  String message = event.getMessage().getContentRaw();

                  int state = values.getState();

                  switch (state)
                  {
                        case 1 -> {
                              var schools = values.getSchoolList();
                              var validMessage = Processor.validateMessage(values, schools);

                              if (!validMessage)
                              {
                                    return;
                              }


                              var school = values.getSchool();


                              Embed.success(event, "** %s ** successfully selected", school.getName());


                              channel.sendMessageFormat("** %s ** successfully has been selected.. This message will be deleted after 3 seconds to reduce clutter", school.getName()).queueAfter(3, TimeUnit.SECONDS, success ->
                              {
                                    success.delete().queue();

                                    var processedList = Processor.processGenericList(values, school.getClassroomList(), Classroom.class);

                                    processClassroomList(values);

                              });

                        }


                        case 3 -> {
                              var classroomList = values.getClassroomList();
                              var success = Processor.validateMessage(values, classroomList);
                              var classroom = values.getClassroom();
                              Embed.success(event, "** %s ** has successfully been selected", classroom.getName());
                              channel.sendMessageFormat("""
                                              Now that we have all that sorted the fun stuff can start %s
                                              Im going to start by asking for your assignment name
                                              """,
                                      Emoji.SMILEY_FACE.getAsChat()).queue();
                        }

                        case 4 -> {
                              values.setAssignment(new Assignment());
                              var classroom = values.getClassroom();

                              values.getAssignment().setClassroom(classroom);
                              values.getAssignment().setName(message);

                              Embed.success(event, "** %s ** has successfully been added as Assignment name..", values.getAssignment().getName());
                              channel.sendMessageFormat("Please give me a small description about the assignment. You can change it later so if you wanna speed through this its fine %s", Emoji.SMILEY_FACE.getAsChat()).queue();

                              values.incrementMachineState();

                        }

                        case 5 -> {
                              values.getAssignment().setDescription(message);

                              Embed.success(event, "Description has successfully been added as Assignment name..");
                              channel.sendMessage("Okay got it im going to need the point amount for the assignment.. If you don't know just put 'idk' or 0").queue();
                              values.incrementMachineState();
                        }

                        case 6 -> {
                              if (!Checks.isNumber(message) || message.toLowerCase().contains("idk"))
                              {
                                    Embed.error(event, "** %s ** is not a number.. try again!", message);
                                    return;
                              }

                              int points = message.toLowerCase().contains("idk") ? 0 : Integer.parseInt(message);

                              values.getAssignment().setPoints(points);
                              Embed.success(event, "** %d ** has been set as ** %s ** point amount", points, values.getAssignment().getName());
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
                              values.incrementMachineState();
                        }

                        case 7 -> {
                              String content = message.toLowerCase();

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

                              values.getAssignment().setType(type);

                              Embed.success(event, "** %s ** has been set as your assignment type", type.getAssignmentType());
                              channel.sendMessage("""
                                      I will need your due date..
                                      Please use the following format: `M/dd/yyyy`
                                      An Example: `2/9/2004`
                                      """).queue();
                              values.incrementMachineState();
                        }

                        case 8 -> {
                              var classroom = values.getClassroom();
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
                                      An Example: `12:30pm` or `8:30am`
                                      """).queue();
                              values.incrementMachineState();
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


                                    values.getAssignment().setDueDate(LocalDateTime.of(date, LocalTime.of(hour, minute)));
                              }
                              else
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("pm", ""));


                                    if (hour == 12)
                                    {
                                          hour = -12;
                                    }

                                    values.getAssignment().setDueDate(LocalDateTime.of(date, LocalTime.of((hour + 12), minute)));
                              }

                              var commandEvent = values.getCommandEvent();
                              var assignment = values.getAssignment();
                              commandEvent.addAssignment(assignment);

                              Embed.success(event, "** %s ** has successfully been added to ** %s **", assignment.getName(), assignment.getClassroom().getName());
                              jda.removeEventListener(this);

                        }
                  }
            }
      }

}
