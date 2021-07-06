package schoolbot.commands.info;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;

import java.util.List;

public class Tutorial extends Command
{
      public Tutorial()
      {
            super("Sends tutorial for bot in channel that is called in", "[none]", 0);
            addCalls("tut", "tutorial");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            EmbedUtils.sendTutorial(event.getChannel());
      }
}
