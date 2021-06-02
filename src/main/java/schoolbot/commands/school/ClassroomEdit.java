package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.util.List;

public class ClassroomEdit extends Command
{

      public ClassroomEdit(Command parent)
      {
            super(parent, "", "", 0);

            addUsageExample("N/A");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

      }
}
