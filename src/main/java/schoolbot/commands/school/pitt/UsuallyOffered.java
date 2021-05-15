package schoolbot.commands.school.pitt;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.util.List;

public class UsuallyOffered extends Command
{
      public UsuallyOffered()
      {
            super("", "", 1);
      }

      /**
       * Command to check when classes are usually offered
       * URL: https://psmobile.pitt.edu/app/catalog/listCoursesBySubject/UPITT/<FIRST LETTER OF SUBJECT>/<SUBJECT>
       * Ex: https://psmobile.pitt.edu/app/catalog/listCoursesBySubject/UPITT/C/CS
       * <p>
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

      }
}
