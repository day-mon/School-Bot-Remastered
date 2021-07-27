package schoolbot.commands.school;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class ListProfessors extends Command
{
      public ListProfessors()
      {
            super("Lists professors when a school is selected", "[none]", 0);
            addCalls("professors", "profs", "lp");
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schools);

            var success = Processor.processGenericListWithSendingList(values, schools, School.class);

            if (success == 0)
            {
                  return;
            }

            if (success == 1)
            {
                  var professorList = values.getSchool().getProfessorList();

                  event.sendAsPaginatorWithPageNumbers(professorList);

                  return;
            }
            jda.addEventListener(new ListProfessorStateMachine(values));

      }

      private static class ListProfessorStateMachine extends ListenerAdapter implements StateMachine
      {

            private final StateMachineValues values;


            public ListProfessorStateMachine(StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var jda = event.getJDA();
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);

                  if (!requirementsMet)
                  {
                        return;
                  }

                  var schoolList = values.getSchoolList();
                  var validated = Processor.validateMessage(values, schoolList);

                  if (!validated)
                  {
                        return;
                  }

                  Processor.processGenericList(values, values.getProfessorList(), Professor.class);

                  jda.removeEventListener(this);
            }
      }

}
