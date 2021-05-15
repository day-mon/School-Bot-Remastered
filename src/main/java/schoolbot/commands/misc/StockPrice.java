package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Checks;
import schoolbot.util.Embed;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.util.List;

public class StockPrice extends Command
{
      public StockPrice(Command parent)
      {
            super(parent, "", "", 1);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String stockSymbol = args.get(0);

            if (Checks.isNumber(stockSymbol))
            {
                  Embed.error(event, "Stock symbols cannot have numbers");
                  return;
            }

            Stock stock = null;

            try
            {
                  stock = YahooFinance.get(stockSymbol);
            }
            catch (Exception e)
            {
                  e.printStackTrace();
            }

            if (stock == null)
            {
                  Embed.error(event, "** %s ** is not a stock symbol on any stock exchange to my knowledge", stockSymbol);
                  return;
            }

            event.sendMessage(
                    new EmbedBuilder()
                            .setTitle(stock.getName())
                            .setDescription("$" + stock.getQuote().getPrice().toEngineeringString())
            );

            {
                  String s = "s";
            }


      }
}
