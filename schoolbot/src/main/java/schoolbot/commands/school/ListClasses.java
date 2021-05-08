package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Embed;

public class ListClasses extends Command
{
      public ListClasses()
      {
            super("", "", 1);
            addCalls("classes", "classlist");
      }


      /**
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */
      @Override
      public void run(CommandEvent event)
      {
            String firstArg = event.getArgs().get(0);

            if (!event.schoolExist(firstArg))
            {
                  Embed.error(event, "** %s ** does not exist", firstArg);
                  return;
            }

            School school = event.getSchool(event, firstArg);

            if (school.getClassesSize() < 0)
            {
                  Embed.error(event, "** %s ** has no classes", school.getSchoolName());
                  return;
            }

            event.getAsPaginator(school.getClassroomList());

      }
}
