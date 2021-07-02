package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.School;
import schoolbot.util.EmbedUtils;

import java.util.List;

public class ListClasses extends Command
{
      public ListClasses()
      {
            super("", "", 1);
            addCalls("classes", "classlist");
            addUsageExample("classes \" University of Pittsburgh \"");
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String firstArg = args.get(0);

            if (!event.schoolExist(firstArg))
            {
                  EmbedUtils.error(event, "** %s ** does not exist", firstArg);
                  return;
            }

            School school = event.getSchool(firstArg);

            if (school.getClassroomList().isEmpty())
            {
                  EmbedUtils.error(event, "** %s ** has no classes", school.getName());
                  return;
            }

            event.sendAsPaginatorWithPageNumbers(school.getClassroomList());
      }
}
