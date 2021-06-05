package schoolbot.commands.misc;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Embed;

import java.util.List;
import java.util.Map;

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


            if (!commands.containsKey(command))
            {
                  Embed.error(event, "** %s ** is not a command", command);
                  return;
            }


            Command cmd = commands.get(command);

            if (args.size() == 2)
            {
                  if (cmd.hasChildren())
                  {
                        for (Command coms : cmd.getChildren())
                        {
                              String child = coms.getName().split(cmd.getName())[1];
                              String potentialChild = args.get(1);

                              if (child.equalsIgnoreCase(potentialChild))
                              {
                                    cmd = coms;
                                    break;
                              }
                        }
                  }
            }


            if (cmd.hasChildren())
            {
                  List<MessageEmbed> embeds = null;
                  cmd.getChildren()
                          .forEach(c0mmand ->
                          {
                                embeds.add(c0mmand.getAsHelpEmbed().build());
                          });

                  event.sendAsPaginator(embeds);

                  event.sendMessage("This is a parent command please try and specifying your search.. Here are its children `%s`", cmd.getChildren());
            }
            else
            {
                  event.sendMessage(cmd.getAsHelpEmbed().build());
            }
      }
}
