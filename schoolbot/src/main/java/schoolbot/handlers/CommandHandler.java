package schoolbot.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Activity.Emoji;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.commands.misc.Ask;
import schoolbot.commands.misc.Eval;
import schoolbot.commands.misc.Format;
import schoolbot.commands.misc.Hello;
import schoolbot.commands.misc.Time;
import schoolbot.commands.*;
import schoolbot.commands.admin.Clear;
import schoolbot.commands.admin.Prune;
import schoolbot.commands.misc.Uptime;
import schoolbot.commands.school.Wolfram;
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
                                new Clear(waiter),
                                new Hello(waiter));

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
        com.process(new CommandEvent(event, com, filteredArgs, schoolbot));



    }


    public Map<String, Command> getCommands() 
    {
        return commands;
    }


}
