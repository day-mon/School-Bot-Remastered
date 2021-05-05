package schoolbot.commands.school.pitt;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class UsuallyOffered extends Command
{
      public UsuallyOffered()
      {
            super("", "", 0);
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
      public void run(CommandEvent event)
      {

      }

}
