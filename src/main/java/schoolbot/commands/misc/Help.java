package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Embed;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Help extends Command
{

      public Help()
      {
            super("Helps users with commands", "[command name]", 1);
            addCalls("help");

      }


      /**
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */


      @Override
      public void run(CommandEvent event, @NotNull List<String> args)
      {
            Map<String, Command> commands = event.getSchoolbot().getCommandHandler().getCommands();
            String command = args.get(0);

            if (commands.containsValue(command))
            {
                  Embed.error(event, "** %s ** is not a command");
                  return;
            }

            Command cmd = commands.get(command);


            if (cmd.hasChildren())
            {
                  // Add Logic for commands with children..
            }
            else
            {
                  event.sendMessage(cmd.getAsHelpEmbed().build());
            }
      }

      private int getDistinctNumber(Set<Integer> set, int maxSize, int num)
      {
            if (set.add(num))
            {
                  return num;
            }
            else
            {
                  return getDistinctNumber(set, maxSize, new Random().nextInt(maxSize));
            }
      }

}
