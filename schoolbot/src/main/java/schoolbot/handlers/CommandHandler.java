package schoolbot.handlers;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.github.classgraph.ClassGraph;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.commands.admin.Clear;
import schoolbot.commands.admin.Eval;
import schoolbot.commands.admin.Prune;
import schoolbot.commands.misc.*;
import schoolbot.commands.school.*;
import schoolbot.commands.school.pitt.*;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.util.Embed;
import schoolbot.natives.util.Parser;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler
{
    private final String COMMANDS_PACKAGE = "schoolbot.commands";
    private final ClassGraph classGraph = new ClassGraph().acceptPackages(COMMANDS_PACKAGE);
    private final Logger CMD_HANDLER_LOGGER = LoggerFactory.getLogger(this.getClass());

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
                new ListSchools(waiter),
                new FindPittStudent(),
                new Professor(),
                new Laundry(),
                new CourseLookUp(),
                new Calender(),
                new ListProfessors());

        Map<String, Command> comsHashMap = new LinkedHashMap<>();
        for (Command com : comList)
        {
            if (com.hasCommandFlags(CommandFlag.INTERNET, CommandFlag.DATABASE))
            {
                com.addCooldown(10000L);
            }
            comsHashMap.put(com.getName(), com);

            for (String aliases : com.getCalls())
            {
                comsHashMap.put(aliases, com);
            }
        }
        return comsHashMap;
    }

    /*
        public Map<String, Command> initCommands()
        {
            Map<String, Command> commandMap = new HashMap<>();

            try (ScanResult result = classGraph.scan())
            {
                for (ClassInfo cls : result.getAllClasses())
                {
                    Constructor<?>[] constructors = cls.loadClass().getConstructors();

                    if (constructors.length == 0)
                    {
                        CMD_HANDLER_LOGGER.warn("No valid constructor found for class [{}]!", cls.getSimpleName());
                        continue;
                    }
                    Constructor<?> currentConstructor = constructors[0];
                    if (currentConstructor.getParameterCount() > 0)
                    {
                        continue;
                    }

                    Object instance = constructors[0].newInstance();

                    if(!(instance instanceof Command))
                    {
                        CMD_HANDLER_LOGGER.warn("{} is a non command class found in the commands package", cls.getSimpleName());
                        continue;
                    }
                    Command command = (Command) instance;

                 //   System.out.println(command.getName() + " has children: " + command.hasChildren());



                    commandMap.put(command.getName(), command);
                    for (String aliases : command.getCalls())
                    {
                        commandMap.put(aliases, command);
                    }
                }
            }
            catch (Exception e)
            {
                CMD_HANDLER_LOGGER.error("Exception occurred.. Exiting in one second", e);
                System.exit(1);
            }

            schoolbot.getLogger().info("{} have been successfully loaded!", commandMap);
            return commandMap;
        }


     */
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
