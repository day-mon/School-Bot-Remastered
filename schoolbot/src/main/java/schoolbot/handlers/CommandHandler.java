package schoolbot.handlers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.commands.misc.*;
import schoolbot.commands.admin.Eval;
import schoolbot.commands.admin.Clear;
import schoolbot.commands.admin.Prune;
import schoolbot.commands.school.*;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Embed;
import schoolbot.natives.util.Parser;

public class CommandHandler 
{
    private final Schoolbot schoolbot;
    private final Map<String, Command> commands;
    private EventWaiter waiter;

    public CommandHandler(Schoolbot schoolbot, EventWaiter waiter) 
    {
        this.schoolbot = schoolbot;
        this.waiter = waiter;
        this.commands = generateCommandsMap();
    }

    private Map<String, Command> generateCommandsMap()
    {
        List<Command> comList = List.of(
                new Uptime(),
                new Wolfram(),
                new Ask(),
                new Format(),
                new Time(),
                new Eval(),
                new Prune(),
                new Google(),
                new Clear(waiter),
                new Hello(waiter),
                new School(waiter),
                new ListElectives(),
                new LeetCode(),
                new ListSchools(waiter),
                new FindPittStudent(),
                new Professor());

        Map<String, Command> comsHashMap = new LinkedHashMap<>();
        for (Command com : comList)
        {
            comsHashMap.put(com.getName(), com);
     
            for (String aliases : com.getCalls())
            {
                comsHashMap.put(aliases, com);
            }
        }
        schoolbot.getLogger().info("{} have been sucessfully loaded!", comList);
        return comsHashMap;
    }

    public void handle(GuildMessageReceivedEvent event)
    {
		String content = event.getMessage().getContentRaw().substring(SchoolbotConstants.DEFAULT_PREFIX.length());

        List<String> filteredArgs = Parser.args(content)
                            .stream()
                            .filter(arg -> !arg.isBlank())
                            .collect(Collectors.toList());

        if (filteredArgs.isEmpty())
        {
            return;
        }

        
        String alias = filteredArgs.get(0).toLowerCase();
        Command com = commands.get(alias);

        if (com == null)
        {
            Embed.error(event, "Command does not exist!");
            return;
        }
        

        /**
         * 2000dec
         */
        // Removing command call 
        filteredArgs.remove(0);
        CommandEvent commandEvent = new CommandEvent(event, com, filteredArgs, schoolbot);

        if (!com.hasChildren())
        {
            com.process(commandEvent);
            return;
        }
        com.getChildren()
                    .stream()
                    .filter(child -> child.getName().split(child.getParent().getName())[1].equalsIgnoreCase(filteredArgs.get(0)))
                    .findFirst()
                    .ifPresentOrElse(
                            child -> child.process(new CommandEvent(event, child, filteredArgs.subList(1, filteredArgs.size()), schoolbot)),
                            () -> com.process(commandEvent)
                    );


    }


    public Map<String, Command> getCommands() 
    {
        return commands;
    }


}
