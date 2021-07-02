package schoolbot.commands.school;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.Classroom;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.util.List;

public class ListAssignments extends Command
{

      public ListAssignments()
      {
            super("List all assignments in a given class", "[none]", 0);
            addCalls("assignments");
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }


      private static void processAssignmentList(StateMachineValues values)
      {
            var commandEvent = values.getCommandEvent();
            var assignmentList = values.getAssignmentList();

            if (assignmentList.isEmpty())
            {
                  var clazz = values.getClassroom();

                  EmbedUtils.error(commandEvent, "There are no assignments for %s", clazz.getName());
            }
            else if (assignmentList.size() == 1)
            {
                  var event = values.getMessageReceivedEvent();
                  var channel = event.getChannel();
                  var assignment = assignmentList.get(0);
                  var schoolbot = commandEvent.getSchoolbot();


                  channel.sendMessageEmbeds(assignment.getAsEmbed(schoolbot)).queue();
            }
            else
            {
                  commandEvent.sendAsPaginatorWithPageNumbers(assignmentList);
            }
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();

            Classroom classroom = Checks.messageSentFromClassChannel(values);


            if (classroom != null)
            {
                  var assignmentList = values.getAssignmentList();

                  processAssignmentList(values);

                  return;
            }


            var classroomList = Processor.processUserRoles(values);

            if (classroomList.isEmpty())
            {
                  EmbedUtils.error(event, "You have no association to any classes, and you did not send this message in any channels associated with a class");
                  return;
            }

            var success = Processor.processGenericList(values, classroomList, Classroom.class, false);

            if (success == 0)
            {
                  return;
            }

            if (success == 1)
            {
                  processAssignmentList(values);

                  return;
            }

            jda.addEventListener(new ListAssignmentsStateMachine(values));


      }


      public static class ListAssignmentsStateMachine extends ListenerAdapter
      {

            private final StateMachineValues values;


            public ListAssignmentsStateMachine(StateMachineValues values)
            {
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

                  int state = values.getState();

                  if (state == 1)
                  {
                        var classroomList = values.getClassroomList();

                        var valid = Processor.validateMessage(values, classroomList);

                        if (!valid)
                        {
                              return;
                        }

                        processAssignmentList(values);
                  }
            }
      }

}
