package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.School;
import schoolbot.util.Embed;

import java.util.List;

public class ListClasses extends Command
{
      public ListClasses()
      {
            super("", "", 1);
            addCalls("classes", "classlist");
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String firstArg = args.get(0);

            if (!event.schoolExist(firstArg))
            {
                  Embed.error(event, "** %s ** does not exist", firstArg);
                  return;
            }

            School school = event.getSchool(event, firstArg);

            if (school.getClassroomList().isEmpty())
            {
                  Embed.error(event, "** %s ** has no classes", school.getSchoolName());
                  return;
            }

            event.getAsPaginatorWithPageNumbers(school.getClassroomList());
      }
}
