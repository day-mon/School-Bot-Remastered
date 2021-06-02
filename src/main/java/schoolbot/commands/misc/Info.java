package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.info.SystemInfo;

import java.lang.management.ManagementFactory;
import java.util.List;

public class Info extends Command
{

      public Info()
      {
            super("info", "Shows some bot info", 0);
            addCalls("info");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var runtime = Runtime.getRuntime();
            var jda = event.getJDA();

            event.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setTitle("Schoolbot Information", "http://schoolbot.dev")
                            .addField("JVM Version:", SystemInfo.getJavaVersion(), true)
                            .addField("JDA Version:", JDAInfo.VERSION, true)
                            .addBlankField(true)


                            .addField("Memory Usage:", ((runtime.totalMemory() - runtime.freeMemory()) >> 20) + "MB / " + (runtime.maxMemory() >> 20) + "MB", true)
                            .addField("Thread Count:", String.valueOf(ManagementFactory.getThreadMXBean().getThreadCount()), true)
                            .addBlankField(true)
                            .build()
            ).queue();
      }
}