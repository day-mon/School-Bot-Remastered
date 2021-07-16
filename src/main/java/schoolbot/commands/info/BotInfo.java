package schoolbot.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.info.SystemInfo;

import java.lang.management.ManagementFactory;
import java.util.List;

public class BotInfo extends Command
{

      public BotInfo()
      {
            super("BotInfo", "Shows some bot info", 0);
            addCalls("botinfo", "binfo");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var runtime = Runtime.getRuntime();
            var jda = event.getJDA();

            event.getChannel().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("Schoolbot Information", "https://schoolbot.dev")
                            .addField("JVM Version:", SystemInfo.getJavaVersion(), true)
                            .addField("JDA Version:", JDAInfo.VERSION, true)
                            .addField("Schoolbot Version", Constants.VERSION, true)
                            .addBlankField(true)

                            .addField("Host OS", SystemInfo.getOperatingSystem(), true)
                            .addField("Memory Usage:", ((runtime.totalMemory() - runtime.freeMemory()) >> 20) + "MB / " + (runtime.maxMemory() >> 20) + "MB", true)
                            .addField("Thread Count:", String.valueOf(ManagementFactory.getThreadMXBean().getThreadCount()), true)
                            .addBlankField(true)

                            .addField("Guild Count:", String.valueOf(jda.getGuildCache().size()), true)
                            .addField("Guild Prefix:", event.getGuildPrefix(), true)
                            .addField("User Count", String.valueOf(event.getJDA()
                                    .getGuilds()
                                    .stream()
                                    .mapToInt(Guild::getMemberCount)
                                    .sum()), true)
                            .addBlankField(true)
                            .build()
            ).queue();
      }
}