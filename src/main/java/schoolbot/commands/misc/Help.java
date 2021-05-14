package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.List;

public class Help extends Command
{

      public Help()
      {
            super("", "", 1);
      }


      /**
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */


      @Override
      public void run(CommandEvent event, @NotNull List<String> args)
      {

      }
}
