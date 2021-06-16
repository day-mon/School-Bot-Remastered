package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
            addUsageExample("N/A");
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES);
            addFlags(CommandFlag.DATABASE);

      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var values = new StateMachineValues(event);
            var jda = event.getJDA();

            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getClassroomList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schools);


            var schoolSuccess = Processor.processGenericList(values, schools, School.class);

            if (schoolSuccess == 1)
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
                        values.setState(3);
                  }

            }


            if (schools.isEmpty())
            {
                  Embed.error(event, "There is no valid schools you can delete.. Schools can only be deleted if they have no **classes** and **professors** assigned to it");
            }
            else if (schools.size() == 1)
            {
                  event.sendMessage(schools.get(0).getAsEmbed(event.getSchoolbot()));
                  event.sendMessage("This is the only school available with deletable classes.");

                  List<Classroom> classrooms = event.getGuildClasses()
                          .stream()
                          .filter(classroom -> classroom.getAssignments().isEmpty())
                          .collect(Collectors.toList());

                  if (classrooms.isEmpty())
                  {
                        Embed.error(event, "There are no classes are available to remove");
                  }
                  else if (classrooms.size() == 1)
                  {
                        event.sendMessage(classrooms.get(0).getAsEmbed(event.getSchoolbot()));
                        event.sendMessage("Are you sure you want to remove this class?");
                        event.getJDA().addEventListener(new ClassroomRemoveStateMachine(event, classrooms.get(0), 3));
                  }
                  else
                  {
                        event.sendMessage("What class would you like to remove?");
                        event.sendAsPaginatorWithPageNumbers(classrooms);
                        event.getJDA().addEventListener(new ClassroomRemoveStateMachine(event, classrooms, schools, 2));
                  }
            }
            else
            {
                  event.sendMessage("Please tell me the school you want to a class from by the page numbers");
                  event.sendAsPaginatorWithPageNumbers(schools);
                  event.getJDA().addEventListener(new ClassroomRemoveStateMachine(event, null, schools, stateToSwitch));
            }
      }

      public static class ClassroomRemoveStateMachine extends ListenerAdapter implements StateMachine
      {
            private final long channelId, authorId;
            private final CommandEvent commandEvent;
            private List<School> schools;
            private List<Classroom> classroomList;
            private int state;
            private Classroom classroom;

            private StateMachineValues values;

            public ClassroomRemoveStateMachine(@NotNull CommandEvent event, @Nullable List<Classroom> classrooms, @NotNull List<School> schools, int stateToSwitchTo)
            {
                  this.commandEvent = event;
                  this.schools = schools;
                  this.state = stateToSwitchTo;
                  this.channelId = event.getChannel().getIdLong();
                  this.authorId = event.getUser().getIdLong();
                  this.classroomList = classrooms;
            }

            public ClassroomRemoveStateMachine(@NotNull CommandEvent event, @NotNull Classroom classroom, int stateToSwitchTo)
            {
                  this.commandEvent = event;
                  this.classroom = classroom;
                  this.state = stateToSwitchTo;
                  this.channelId = event.getChannel().getIdLong();
                  this.authorId = event.getUser().getIdLong();
            }

            public ClassroomRemoveStateMachine(@NotNull StateMachineValues values)
            {
                  this.values = values;

            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  var channelId = event.getChannel().getIdLong();
                  var userId = event.getMessage().getIdLong();
                  var requirementsMet = Checks.eventMeetsPrerequisites(event, this, channelId, userId);

                  if (!requirementsMet)
                  {
                        return;
                  }


                  String message = event.getMessage().getContentRaw();
                  MessageChannel channel = event.getChannel();
                  JDA jda = event.getJDA();

                  if (message.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Okay aborting... ").queue();
                        jda.removeEventListener(this);
                        return;
                  }

                  switch (state)
                  {
                        case 1 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, "[ ** %s ** ] is not a number", message);
                                    return;
                              }

                              int index = Integer.parseInt(message) - 1;

                              School school = schools.get(index);
                              this.classroomList = school.getClassroomList()
                                      .stream()
                                      .filter(classroom -> !classroom.getAssignments().isEmpty())
                                      .collect(Collectors.toList());

                              if (classroomList.isEmpty())
                              {
                                    Embed.error(commandEvent, "** %s ** has no classes without assignments already assigned to it", school.getName());
                                    jda.removeEventListener(this);
                                    return;
                              }
                              Embed.success(event, "** %s ** has been selected", school.getName());
                              commandEvent.sendAsPaginatorWithPageNumbers(classroomList);
                              Embed.information(commandEvent, "Please select a page number of the class you want to remove");

                              state = 2;
                        }


                        case 2 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, "** %s ** is not number... Try again", message);
                                    return;
                              }

                              int index = Integer.parseInt(message) - 1;

                              if (!Checks.between(index + 1, this.classroomList.size()))
                              {
                                    Embed.error(event, "** %s ** is not between 1 - %d", message, this.classroomList.size());
                                    return;
                              }

                              this.classroom = classroomList.get(index);


                              channel.sendMessageFormat("Are you sure you want to remove ** %s **", this.classroom.getName()).queue();
                              state = 3;
                        }

                        case 3 -> {
                              if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y"))
                              {
                                    commandEvent.removeClass(commandEvent, this.classroom);
                                    Embed.success(event, "Removed [** %s **] successfully", this.classroom.getName());
                                    event.getJDA().removeEventListener(this);
                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("nah"))
                              {
                                    channel.sendMessage("Okay.. aborting..").queue();
                                    event.getJDA().removeEventListener(this);
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
