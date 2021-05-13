package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.List;

public class Stock extends Command
{

      public Stock()
      {
            super("", "", 1);
            addChildren(
                    new StockPrice(this),
                    new StockQuote(this)
            );
            addCalls("stock", "stonk", "stonks");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

      }
}
