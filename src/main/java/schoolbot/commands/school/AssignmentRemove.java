package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class AssignmentRemove extends Command
{
      /**
       * @param parent Parent command of [Assignment]
       */
      public AssignmentRemove(Command parent)
      {
            super(parent, "Removes an assignment from a class", "[none]", 0);
            setCommandPrerequisites("A valid assignment to edit");
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {

            var jda = event.getJDA();
            var schoolList = values.getSchoolList()
                    .stream()
                    .filter(School::hasClasses)
                    .collect(Collectors.toList());
            var channel = event.getTextChannel();
            Classroom classroom = Checks.messageSentFromClassChannel(values);

            if (classroom != null)
            {
                  EmbedUtils.information(event,
                          "** %s ** has been selected automatically because the message came from %s", classroom.getName(), channel.getAsMention());

                  var processedList = Processor.processGenericList(values, values.getAssignmentList(), Assignment.class);

                  if (processedList == 0)
                  {
                        return;
                  }

                  if (processedList == 1)
                  {
                        var assignment = values.getAssignment();

                        EmbedUtils.information(event, "** %s ** has been selected because it is the only deletable assignment. Would you like to remove it?", assignment.getName());
                        values.setState(5);
                        return;
                  }

                  values.setState(5);
                  jda.addEventListener(new AssignmentRemoveMachine(values));
            }

      }

      public static class AssignmentRemoveMachine extends ListenerAdapter implements StateMachine
      {

            private final StateMachineValues values;

            public AssignmentRemoveMachine(@NotNull StateMachineValues values)
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

                  MessageChannel channel = event.getChannel();
                  String message = event.getMessage().getContentRaw();
                  JDA jda = event.getJDA();
                  Guild guild = event.getGuild();


                  int state = values.getState();

                  switch (state)
                  {
                        case 1 -> {
                              if (!Checks.isNumber(message))
                              {
                                    EmbedUtils.error(event, """
                                            ** %s ** is not a number
                                            Please Enter a number
                                            """, message);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(message);

                              if (!Checks.between(pageNumber, schools.size()))
                              {
                                    EmbedUtils.error(event, "** %s ** was not one of the school ids...", message);
                                    return;
                              }

                              classroom.setSchool(schools.get(pageNumber - 1));
                              EmbedUtils.success(event, "** %s ** successfully selected", classroom.getSchool().getName());
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

                              classroomList = commandEvent.getSchool(classroom.getSchool().getName()).getClassroomList()
                                      .stream()
                                      .filter(classroom -> !classroom.getAssignments().isEmpty())
                                      .collect(Collectors.toList());

                              if (classroomList.isEmpty())
                              {
                                    EmbedUtils.error(event, "** %s ** does not have any classes associated with it", guild.getName());
                                    jda.removeEventListener(this);
                              }
                              else if (classroomList.size() == 1)
                              {
                                    EmbedUtils.success(event, "** %s ** has been selected automatically because you only have one class associated with you!", classroomList.get(0).getName());
                                    channel.sendMessageFormat("""
                                                
                                            """, Emoji.SMILEY_FACE.getAsChat()
                                    ).queue();
                                    this.classroom = classroomList.get(0);
                                    state = 4;
                              }
                              else
                              {
                                    commandEvent.sendAsPaginatorWithPageNumbers(classroomList);
                                    channel.sendMessage("Please give me the page number of the class you want to remove the assignment from").queue();
                              }
                        }

                        case 3 -> {
                              if (!Checks.isNumber(message))
                              {
                                    EmbedUtils.error(event, "** %s ** is not a valid entry", message);
                                    return;
                              }

                              int index = Integer.parseInt(message) - 1;

                              if (!Checks.between(index + 1, classroomList.size()))
                              {
                                    EmbedUtils.error(event, "** %s ** was not one of the class ids...", message);
                                    event.getJDA().removeEventListener(this);
                                    return;
                              }
                              this.classroom = classroomList.get(index);

                              EmbedUtils.success(event, "** %s ** has successfully been selected", this.classroom.getName());

                              this.assignmentList = classroom.getAssignments();
                              if (assignmentList.isEmpty())
                              {
                                    EmbedUtils.error(event, "** %s ** has no assignments.", classroom.getName());
                                    return;
                              }
                              else if (assignmentList.size() == 1)
                              {
                                    Assignment assignment = assignmentList.get(0);

                                    channel.sendMessageEmbeds(assignment.getAsEmbed(commandEvent.getSchoolbot())).queue();
                                    channel.sendMessageFormat("** %s ** is the only assignment.. Would you like to delete it?", assignment.getName()).queue();
                                    state = 5;
                              }
                              else
                              {
                                    commandEvent.sendAsPaginatorWithPageNumbers(assignmentList);
                                    channel.sendMessage("Please select the assignment by page number").queue();
                              }
                              state = 4;
                        }

                        case 4 -> {
                              if (!Checks.isNumber(message))
                              {
                                    EmbedUtils.error(event, """
                                            ** %s ** is not a number
                                            Please Enter a number
                                            """, message);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(message);

                              if (!Checks.between(pageNumber, assignmentList.size()))
                              {
                                    EmbedUtils.error(event, "** %s ** was not one of the assignment page numbers...", message);
                                    return;
                              }

                              this.assignment = assignmentList.get(pageNumber - 1);
                              channel.sendMessageFormat("Are you sure you want to delete ** %s ** ?", this.assignment.getName()).queue();
                              state = 5;
                        }

                        case 5 -> {
                              if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y"))
                              {
                                    commandEvent.removeAssignment(assignment);
                                    EmbedUtils.success(event, "Removed [** %s **] successfully", this.assignment.getName());
                                    event.getJDA().removeEventListener(this);
                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("nah"))
                              {
                                    channel.sendMessage("Okay.. aborting..").queue();
                                    event.getJDA().removeEventListener(this);
                              }
                              else
                              {
                                    EmbedUtils.error(event, "[ ** %s ** ] is not a valid respond.. I will need a **Yes** OR a **No**", message);
                              }
                        }
                  }
            }
      }
}
