package schoolbot.commands.misc;

import schoolbot.objects.command.Command;

public class Prefix extends Command
{
      public Prefix()
      {
            super("Prefix Command", "Prefix Command", 1);
            addCalls("prefix", "pfix");
            addChildren(
                    new PrefixSet(this),
                    new PrefixView(this)
            );
      }
}
