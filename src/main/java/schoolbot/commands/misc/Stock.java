package schoolbot.commands.misc;

import schoolbot.objects.command.Command;

public class Stock extends Command
{

      public Stock()
      {
            super("Gives a price or a quote of a given security", "[quote/price] [stock]", 1);
            addChildren(
                    new StockPrice(this),
                    new StockQuote(this)
            );
            addCalls("stock", "stonk", "stonks");
            addUsageExample("stock [quote/price] [stock]");
      }
}
