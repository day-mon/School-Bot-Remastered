package schoolbot.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import schoolbot.handlers.CommandCooldownHandler;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.util.List;

public class Eval extends Command
{


      private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("groovy");
      private static final List<String> DEFAULT_IMPORTS = List.of("net.dv8tion.jda.api.entities.impl", "net.dv8tion.jda.api.managers", "net.dv8tion.jda.api.entities", "net.dv8tion.jda.api",
              "java.io", "java.math", "java.util", "java.util.concurrent", "java.time", "java.util.stream");

      public Eval()
      {
            super("Evaluates Java code.", "[code]", 1);
            addPermissions(Permission.ADMINISTRATOR);
            addCalls("eval", "evaluate", "code");
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Object out;
            String status = "Success";


            SCRIPT_ENGINE.put("e", event);
            SCRIPT_ENGINE.put("message", event.getMessage());
            SCRIPT_ENGINE.put("channel", event.getChannel());
            SCRIPT_ENGINE.put("args", args);
            SCRIPT_ENGINE.put("jda", event.getJDA());
            SCRIPT_ENGINE.put("author", event.getUser());
            SCRIPT_ENGINE.put("guild", event.getGuild());


            StringBuilder imports = new StringBuilder();
            DEFAULT_IMPORTS.forEach(imp -> imports.append("import ").append(imp).append(".*; "));
            String code = String.join(" ", args);
            long start = System.currentTimeMillis();
            String stuff = "";

            try
            {
                  out = SCRIPT_ENGINE.eval(imports + code);
            }
            catch (Exception exception)
            {
                  out = exception.getMessage();
                  status = "Error found";
            }

            Color color = status.equals("Error found") ? Color.RED : Color.GREEN;

            event.sendMessage(new EmbedBuilder()
                            .setTitle("Evaluated Result")
                            .addField("Status:", status, true)
                            .addField("Duration:", (System.currentTimeMillis() - start) + "ms", true)
                            .addField("Code:", "```java\n" + code + "\n```", false)
                            .addField("Result:", out == null ? stuff : out.toString(), false),
                    color);
            CommandCooldownHandler.addCooldown(event.getMember(), this);
      }
}
