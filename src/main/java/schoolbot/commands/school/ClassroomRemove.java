package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.StateMachine;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class ClassroomRemove extends Command
{

      public ClassroomRemove(Command parent)
      {
            super(parent, "Removes a class from a school", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES);
            addFlags(CommandFlag.DATABASE, CommandFlag.STATE_MACHINE_COMMAND);

      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();

            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getClassroomList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schools);


            var schoolSuccess = Processor.processGenericList(values, schools, School.class);

            if (schoolSuccess == 0)
            {
                  return;
            }
            else if (schoolSuccess == 1)
            {
                  event.sendMessage("This is the only school available with deletable classes.");
                  List<Classroom> classrooms = event.getGuildClasses()
                          .stream()
                          .filter(classroom -> classroom.getAssignments().isEmpty())
                          .collect(Collectors.toList());
                  values.setClassroomList(classrooms);

                  var classSuccess = Processor.processGenericList(values, classrooms, Classroom.class);

                  if (classSuccess == 1)
                  {
                        event.sendMessage("Are you sure you want to remove this class?");
                        values.setState(3);
                  }
                  else if (classSuccess == 2)
                  {
                        values.setState(2);
                  }
            }

            // no need to set state to one because its defaulted to one.


            jda.addEventListener(new ClassroomRemoveStateMachine(values));

      }

      public static class ClassroomRemoveStateMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;


            public ClassroomRemoveStateMachine(@NotNull StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);
                  var channel = event.getChannel();
                  var jda = event.getJDA();

                  int state = values.getState();

                  if (!requirementsMet)
                  {
                        return;
                  }

                  String message = event.getMessage().getContentRaw();


                  switch (state)
                  {
                        case 1 -> {
                              var success = Processor.processGenericList(values, values.getSchoolList(), School.class);

                              var classroomList = values.getSchool()
                                      .getClassroomList()
                                      .stream()
                                      .filter(clazzroom -> !clazzroom.hasAssignments())
                                      .collect(Collectors.toList());

                              var school = values.getSchool();


                              if (classroomList.isEmpty())
                              {
                                    Embed.error(event, "** %s ** has no classes without assignments already assigned", school.getName());
                                    jda.removeEventListener(this);
                              }

                              Embed.success(event, "** %s ** has been selected", school.getName());

                              var commandEvent = values.getCommandEvent();

                              commandEvent.sendAsPaginatorWithPageNumbers(classroomList);
                              Embed.information(commandEvent, "Please select a page number of the class you want to remove");
                              values.incrementMachineState();
                        }


                        case 2 -> {

                              var success = Processor.validateMessage(event, values.getClassroomList());

                              if (success == null)
                              {
                                    return;
                              }

                              values.setClassroom(success);

                              channel.sendMessageFormat("Are you sure you want to remove ** %s **", success.getName()).queue();
                              values.incrementMachineState();
                        }

                        case 3 -> {

                              var commandEvent = values.getCommandEvent();
                              var classroom = values.getClassroom();

                              if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y"))
                              {
                                    commandEvent.removeClass(values.getClassroom());
                                    Embed.success(event, "Removed [** %s **] successfully", classroom.getName());
                                    jda.removeEventListener(this);
                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("nah"))
                              {
                                    channel.sendMessage("Okay.. aborting..").queue();
                                    jda.removeEventListener(this);
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
