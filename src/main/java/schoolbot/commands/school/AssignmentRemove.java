package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;

import java.util.List;
import java.util.stream.Collectors;

public class AssignmentRemove extends Command
{
      /**
       * @param parent
       */
      public AssignmentRemove(Command parent)
      {
            super(parent, "Removes an assignment from a class", "[none]", 1);

      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();

            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> school.getClassesSize() > 0)
                    .collect(Collectors.toList());
            Classroom classroom = null;

            if (!member.hasPermission(Permission.ADMINISTRATOR))
            {
                  classroom = Checks.messageSentFromClassChannel(event);

                  if (classroom != null)
                  {
                        event.sendMessage("""
                                ** %s ** has been selected because it you sent it from this channel
                                Please give me the name of the assignment!
                                """, classroom.getClassName());
                  }
                  else
                  {

                  }
            }
      }
}
