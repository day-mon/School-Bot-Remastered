package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Checks;

import java.util.List;

public class ListAssignments extends Command
{

      public ListAssignments()
      {
            super("description", "syntax", 0);
      }


      /**
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */
      @Override
      public void run(CommandEvent event)
      {
            int args = event.getArgs().size();

            if (args == 0)
            {
                  List<Long> roles = Checks.checkRoles(event.getMember());
            }
            else if (args == 1)
            {

            }
      }
}
