package schoolbot.commands.misc;

import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Map<String, Command> commands = event.getSchoolbot().getCommandHandler().getCommands();
            String command = args.get(0);
            var channel = event.getChannel();
            var preifx = event.getSchoolbot().getWrapperHandler().fetchGuildPrefix(event.getGuild().getIdLong());


            if (!commands.containsKey(command))
            {
                  EmbedUtils.error(event, "** %s ** is not a command", command);
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
                  List<SelectOption> selectOptions = new ArrayList<>();







                  var comHasMethod = cmd.getClass().getDeclaredMethods().length != 0;

                  if (comHasMethod)
                  {
                        selectOptions.add(SelectOption.of(cmd.getName(), cmd.getName()));
                  }

                  for (Command child : cmd.getChildren())
                  {
                        selectOptions.add(SelectOption.of(child.getName(), child.getName()));
                  }

                  SelectionMenu menu = SelectionMenu.create("menu:class")
                          .setPlaceholder("Choose the command")
                          .setRequiredRange(1, 1)
                          .addOptions(selectOptions)
                          .build();

                  Command finalCmd = cmd;


                  channel.sendMessage("You asked for help on a parent command. Please choose one of its children!")
                          .setActionRows(ActionRow.of(menu))
                          .queue(message ->
                          {
                                var eventWaiter = event.getSchoolbot().getEventWaiter();
                                eventWaiter.waitForEvent(SelectionMenuEvent.class,
                                        selectionMenuEvent -> selectionMenuEvent.getMember().getIdLong() == event.getMember().getIdLong() &&
                                                              event.getChannel().getIdLong() == channel.getIdLong(),
                                        selectionMenuEvent ->
                                        {
                                              var commandChosen = selectionMenuEvent.getValues().get(0);

                                              if (commandChosen.equals(finalCmd.getName()))
                                              {
                                                    message.delete().queue();
                                                    event.sendMessage(finalCmd.getAsHelpEmbed(preifx));
                                                    return;
                                              }


                                              for (Command comm : finalCmd.getChildren())
                                              {

                                                    var commandName = comm.getName();
                                                    if (commandName.equals(commandChosen))
                                                    {
                                                          message.delete().queue();
                                                          event.sendMessage(comm.getAsHelpEmbed(preifx));
                                                          return;
                                                    }
                                              }
                                        });
                          });
                  return;
            }

            event.sendMessage(cmd.getAsHelpEmbed(preifx).build());

      }
}
