package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.util.List;

public class AssignmentEdit extends Command
{
      public AssignmentEdit(Command parent)
      {
            super(parent, " ", " ", 1);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

      }
}
