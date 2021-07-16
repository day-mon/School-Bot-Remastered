package schoolbot.commands.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
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

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class AssignmentRemove extends Command
{
      /**
       * @param parent Parent command of [Assignment]
       */
      public AssignmentRemove(Command parent)
      {
            super(parent, "Removes an assignment from a class", "[none]", 0);
            addCommandPrerequisites("A valid assignment to edit");
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
//todo fix this
            var jda = event.getJDA();
            var memberIsAdmin = event.getMember().hasPermission(Permission.ADMINISTRATOR);
            var schoolList = values.getSchoolList()
                    .stream()
                    .filter(School::hasAssignments)
                    .collect(Collectors.toList());
            values.setSchoolList(schoolList);
            var channel = event.getTextChannel();
            Classroom classroom = Checks.messageSentFromClassChannel(values);

            if (classroom != null)
            {
                  var processedList = Processor.processGenericList(values, values.getAssignmentList(), Assignment.class);

                  if (processedList == 0)
                  {
                        return;
                  }

                  EmbedUtils.information(event,
                          "** %s ** has been selected automatically because the message came from %s", classroom.getName(), channel.getAsMention());

                  if (processedList == 1)
                  {
                        sendConfirmationMessage(values);
                        return;
                  }

                  values.setState(3);
                  jda.addEventListener(new AssignmentRemoveMachine(values));
                  return;
            }

            if (!memberIsAdmin)
            {
                  EmbedUtils.error(event, "You must either send this command in a classroom channel or ask an admin to remove the assignment");
                  return;
            }


            var processedList = Processor.processGenericList(values, schoolList, School.class);

            if (processedList == 0)
            {
                  return;
            }

            if (processedList == 1)
            {
                  var school = values.getSchool();

                  EmbedUtils.information(event, "**%s** has been chosen because it is the only school that contains classes with assignments", school.getName());
                  values.setState(2);
            }

            jda.addEventListener(new AssignmentRemoveMachine(values));
      }

      public static void sendConfirmationMessage(StateMachineValues values)
      {
            var assignment = values.getAssignment();
            var event = values.getCommandEvent();
            var channel = values.getCommandEvent().getChannel();
            var jda = event.getJDA();
            var stateMachine = values.getMachine();

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Confirmation")
                    .setDescription(String.format("Are you sure you would like to delete **%s**?", assignment.getName()))
                    .setColor(Color.YELLOW)
                    .build()
            ).queue(prompt ->
            {

                  prompt.addReaction(Emoji.CROSS_MARK.getAsReaction()).queue();
                  prompt.addReaction(Emoji.WHITE_CHECK_MARK.getAsReaction()).queue();

                  var eventWaiter = event.getSchoolbot().getEventWaiter();

                  eventWaiter.waitForEvent(MessageReactionAddEvent.class,
                          reactionEvent -> reactionEvent.getMessageIdLong() == prompt.getIdLong()
                                           && Objects.equals(reactionEvent.getUser(), event.getUser()),

                          messageReactionAddEvent ->
                          {
                                var reactionName = messageReactionAddEvent.getReactionEmote().getName();
                                if (reactionName.equals(Emoji.WHITE_CHECK_MARK.getAsReaction()))
                                {
                                      event.removeAssignment(assignment);
                                      EmbedUtils.success(event, "Removed [** %s **] successfully", assignment.getName());
                                }
                                else if (reactionName.equals(Emoji.CROSS_MARK.getAsReaction()))
                                {
                                      EmbedUtils.error(event, "Okay aborting!");
                                }
                                else
                                {
                                      EmbedUtils.error(event, "You attempted to add another reaction to the message... Aborting!");
                                }


                                if (stateMachine != null)
                                {
                                      jda.removeEventListener(stateMachine);
                                }

                          });
            });
      }

      private static class AssignmentRemoveMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;

            private AssignmentRemoveMachine(@NotNull StateMachineValues values)
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

                              var classroomList = school.getClassroomList()
                                      .stream()
                                      .filter(Classroom::hasAssignments)
                                      .collect(Collectors.toList());

                              var processedList = Processor.processGenericList(values, classroomList, Classroom.class);

                              if (processedList == 0)
                              {
                                    return;
                              }

                              if (processedList == 1)
                              {
                                    var classroom = values.getClassroom();
                                    EmbedUtils.information(event, """
                                            **%s** has been automatically chosen because it is the only class that has assignments.
                                            Please give me the page number of the assignment you'd like to remove!
                                            """, classroom.getName());
                                    commandEvent.sendAsPaginatorWithPageNumbers(values.getAssignmentList());
                                    values.setState(3);
                                    return;
                              }


                              values.incrementMachineState();
                        }


                        case 2 -> {
                              var success = Processor.validateMessage(values, values.getClassroomList());

                              if (!success)
                              {
                                    return;
                              }

                              var processedList = Processor.processGenericList(values, values.getAssignmentList(), Assignment.class);

                              if (processedList == 0)
                              {
                                    return;
                              }

                              if (processedList == 1)
                              {
                                    sendConfirmationMessage(values);
                                    return;
                              }

                              values.incrementMachineState();
                        }

                        case 3 -> {
                              var success = Processor.validateMessage(values, values.getAssignmentList());

                              if (!success)
                              {
                                    return;
                              }

                              sendConfirmationMessage(values);
                        }
                  }
            }
      }
}
