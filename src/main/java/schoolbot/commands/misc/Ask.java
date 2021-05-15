package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.util.List;

public class Ask extends Command
{
      public Ask()
      {
            super("Command to call when someone ask a non-valid question", "[none]", 0);
            addCalls("ask");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            event.sendMessage("Refer to: https://dontasktoask.com/");

      }
}
