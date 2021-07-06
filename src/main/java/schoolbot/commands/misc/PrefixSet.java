package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;

import java.util.List;

public class PrefixSet extends Command
{
      public PrefixSet(Command parent)
      {
            super(parent, "Given a prefix, this command will set the guilds prefix", "[prefix]", 1);
            addUsageExample("prefix set bot!");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var firstArg = args.get(0);

            if (firstArg.isBlank())
            {
                  EmbedUtils.error(event, "You cannot set a empty string as your prefix");
                  return;
            }

            if (firstArg.length() > 5)
            {
                  EmbedUtils.error(event, "You cannot have a prefix greater than 5 characters");
                  return;
            }

            var successful = event.assignPrefix(firstArg);

            if (!successful)
            {
                  EmbedUtils.error(event, """
                          Database Error whilst updating prefix
                          Prefix is still your previous prefix: %s
                          """, event.getGuildPrefix());
                  return;
            }

            EmbedUtils.success(event, """
                    Prefix successfully set to %s.
                    You can view this prefix by doing %sprefix view
                    """, firstArg, firstArg);
      }
}
