package schoolbot.handlers;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandCategory;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Parser;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CommandHandler
{
      private static final String COMMANDS_PACKAGE = "schoolbot.commands";
      private final Logger CMD_HANDLER_LOGGER = LoggerFactory.getLogger(this.getClass());

      private final ClassGraph classGraph = new ClassGraph().acceptPackages(COMMANDS_PACKAGE);
      private final ExecutorService executor = Executors.newScheduledThreadPool(5, runnable -> new Thread(runnable, "SchoolBot Command-Thread"));
      private final Schoolbot schoolbot;
      private final Map<String, Command> commands;

      public CommandHandler(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
            this.commands = initCommands();
      }


      public Map<String, Command> initCommands()
      {

            Map<String, Command> commandMap = new LinkedHashMap<>();
            int uniqueCommands = 0;

            try (ScanResult result = classGraph.scan())
            {
                  for (ClassInfo cls : result.getAllClasses())
                  {
                        Constructor<?>[] constructors = cls.loadClass().getDeclaredConstructors();

                        if (constructors.length == 0)
                        {
                              CMD_HANDLER_LOGGER.error("No valid constructor found for class [{}]!", cls.getSimpleName());
                              continue;
                        }


                        if (constructors[0].getParameterCount() > 0)
                        {
                              continue;
                        }

                        Object instance = constructors[0].newInstance();

                        if (!(instance instanceof Command command))
                        {
                              CMD_HANDLER_LOGGER.warn("[{}] is a non command class found in the commands package it can be found in [{}]", cls.getSimpleName(), cls.getPackageName());
                              continue;
                        }


                        commandMap.put(command.getName(), command);
                        uniqueCommands++;

                        command.setCategory(getCommandCategory(command));

                        for (String aliases : command.getCalls())
                        {
                              commandMap.put(aliases, command);
                        }
                  }
            }
            catch (Exception e)
            {
                  CMD_HANDLER_LOGGER.error("Error occurred whilst loading commands, exiting.", e);
                  System.exit(1);
            }

            CMD_HANDLER_LOGGER.info("[{}] commands have been successfully loaded!", uniqueCommands);
            return commandMap;
      }

      private CommandCategory getCommandCategory(Command command)
      {
            var cmd = command.getClass().getPackageName().split("\\.")[2];

            return switch (cmd)
                    {
                          case "admin" -> CommandCategory.ADMIN;
                          case "dev" -> CommandCategory.DEV;
                          case "fun" -> CommandCategory.FUN;
                          case "info" -> CommandCategory.INFO;
                          case "misc" -> CommandCategory.MISC;
                          case "school" -> CommandCategory.SCHOOL;
                          default -> CommandCategory.UNKNOWN;
                    };


      }


      public void handle(GuildMessageReceivedEvent event, String prefix)
      {

            String message = event.getMessage().getContentRaw();

            if (!message.startsWith(prefix))
            {
                  return;
            }

            message = message.substring(prefix.length());


            List<String> filteredArgs = Parser.args(message)
                    .stream()
                    .filter(arg -> !arg.isBlank())
                    .collect(Collectors.toList());


            if (filteredArgs.isEmpty())
            {
                  return;
            }

            String alias = filteredArgs.get(0).toLowerCase();

            if (alias.isBlank() || alias.startsWith(prefix))
            {
                  return;
            }


            Command com = commands.get(alias);


            if (message.contains("”"))
            {
                  EmbedUtils.error(event,
                          """
                                  We notice you are using the `”` character which means you are on an iPhone..
                                  In order to use the correct quotes hold down on those quotes and find the straight ones
                                             """);
                  return;
            }

            if (com == null)
            {
                  return;
            }


            filteredArgs.remove(0);


            CommandEvent commandEvent = new CommandEvent(event, com, filteredArgs, schoolbot, executor);


            // If someone sends a parent command or doesnt have any children
            if (!com.hasChildren() || filteredArgs.isEmpty())
            {
                  //executor.execute(() -> com.process(commandEvent));
                  com.process(commandEvent);
                  return;
            }

            com.getChildren()
                    .stream()
                    .filter(child -> child.getName().split(child.getParent().getName())[1].equalsIgnoreCase(filteredArgs.get(0)))
                    .findFirst()
                    .ifPresentOrElse(
                            child -> executor.execute(() -> child.process(new CommandEvent(event, child, filteredArgs.subList(1, filteredArgs.size()), schoolbot, executor))),
                            () -> executor.execute(() -> com.process(commandEvent))
                    );
      }

      public Map<String, Command> getCommands()
      {
            return commands;
      }

      public List<Command> getFilteredCommands()
      {
            return commands.values().stream()
                    .distinct()
                    .collect(Collectors.toList());

      }


}
