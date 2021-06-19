package schoolbot.commands.school;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class ListProfessors extends Command
{
      public ListProfessors()
      {
            super("", "", 0);
            addCalls("professors", "profs", "lp");
            addFlags(CommandFlag.DATABASE);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var jda = event.getJDA();

            StateMachineValues values = new StateMachineValues(event);
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schools);

            var success = Processor.processGenericList(values, schools, School.class, false);

            if (success == 0)
            {
                  return;
            }
            jda.addEventListener(new ListProfessorStateMachine(values));

      }

      public static class ListProfessorStateMachine extends ListenerAdapter
      {

            private final StateMachineValues values;


            public ListProfessorStateMachine(StateMachineValues values)
            {
                  this.values = values;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var channel = event.getChannel();
                  var schoolbot = values.getEvent().getSchoolbot();
                  var jda = event.getJDA();
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);

                  if (!requirementsMet)
                  {
                        return;
                  }

                  var schoolList = values.getSchoolList();

                  var validation = Processor.validateMessage(event, schoolList);

                  var professorList = validation.getProfessorList();

                  if (professorList.size() == 1)
                  {
                        var professor = professorList.get(0);
                        channel.sendMessageEmbeds(professor.getAsEmbed(schoolbot)).queue();
                  }
                  else
                  {
                        var commandEvent = values.getEvent();
                        commandEvent.sendAsPaginatorWithPageNumbers(professorList);
                  }

                  jda.removeEventListener(this);
            }
      }

}
