package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;

import java.util.List;

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

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            EmbedUtils.information(event, """
                    Hello %s. Thank you for using Schoolbot. 
                                        
                    It looks like you  have called the stock command. The stock command has two children
                    ** quote ** and ** price **. To call them, do the following.
                                        
                    [prefix] stock quote AMZ
                    [prefix] stock price AMZ
                    """, event.getUser().getAsMention());
      }
}
