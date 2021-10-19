package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.StringUtils;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.time.LocalDate;
import java.util.List;

public class StockQuote extends Command
{

      public StockQuote(Command parent)
      {
            super(parent, "Gives information about a given security", "[security]", 1);
            addUsageExample("stock quote MSFT");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String stockSymbol = args.get(0);

            if (Checks.isNumber(stockSymbol))
            {
                  EmbedUtils.error(event, "Stock symbols cannot have numbers");
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
                  EmbedUtils.error(event, "** %s ** is not a stock symbol on any stock exchange to my knowledge", stockSymbol);
                  return;
            }

            yahoofinance.quotes.stock.StockQuote stockQuote = stock.getQuote();

            event.sendMessage(new EmbedBuilder()
                    .setTitle(stockQuote.getSymbol() + " Quote")
                    .addField("Company Name", stock.getName(), false)
                    .addField("Symbol", stock.getSymbol(), false)
                    .addField("Exchange", stock.getStockExchange(), false)
                    .addBlankField(false)
                    .addField("Normal Trading Data", "", false)
                    .addField("Price", stockQuote.getPrice() == null ? "N/A" : '$' + StringUtils.parseNumberWithCommas(stockQuote.getPrice().toPlainString()), false)
                    .addField("Today's Change", stockQuote.getChange() == null ? "N/A" : '$' + stockQuote.getChange().toPlainString(), false)
                    .addField("Average Volume", stockQuote.getAvgVolume() == null ? "N/A" : StringUtils.parseNumberWithCommas(stockQuote.getAvgVolume()), false)
                    .addField("Previous Close", stockQuote.getPreviousClose() == null ? "N/A" : StringUtils.parseNumberWithCommas(stockQuote.getPreviousClose()), false)
                    .addField("Market Cap", stock.getStats().getMarketCap() == null ? "N/A" : StringUtils.parseNumberWithCommas(stock.getStats().getMarketCap()), false)
                    .addField("52 Week High", stockQuote.getYearHigh() == null ? "N/A" : StringUtils.parseNumberWithCommas(stockQuote.getYearHigh()), false)
                    .addField("52 Week Low", stockQuote.getYearLow() == null ? "N/A" : stockQuote.getYearLow().toPlainString(), false)
                    .addField("52 Week Change (High)", stockQuote.getChangeFromYearHigh() == null ? "N/A" : '$' + stockQuote.getChangeFromYearHigh().toPlainString(), false)
                    .setFooter("Live data as of " + StringUtils.formatDate(LocalDate.now()))
            );
      }
}
