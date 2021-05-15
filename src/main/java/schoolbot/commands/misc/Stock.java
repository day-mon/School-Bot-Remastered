package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

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
