package schoolbot.commands.misc;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.handlers.CommandCooldownHandler;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.util.List;

public class Google extends Command
{
      public Google()
      {
            super("Looks up google query", "[query] <index number>", 1);
            addCalls("g", "google", "duck");
            addFlags(CommandFlag.INTERNET);
            addUsageExample("g \"How do trains go so fast\"");
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String baseUrl = "https://duckduckgo.com/?q=";
            String replacedStuff = args.get(0).replaceAll("\\s", "+");
            baseUrl += replacedStuff;

            if (args.size() == 1)
            {
                  try
                  {
                        Document doc = Jsoup.connect(baseUrl)
                                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .referrer("http://www.google.com")
                                .get();

                        String toSend = String.format("http://%s - %s - %s", doc.getElementsByClass("link-text").get(0).text(), doc.select("a[href]").get(0).text(), doc.getElementsByClass("result-snippet").get(0).text());

                        event.sendMessage(toSend);
                        CommandCooldownHandler.addCooldown(event.getMember(), this);

                  }
                  catch (Exception e)
                  {
                        event.sendMessage("No results found for `" + args.get(0) + "`");
                  }
            }
            else
            {
                  if (!Checks.isNumber(args.get(1)))
                  {
                        Embed.error(event, "Not a number");
                        return;
                  }
                  int num = Integer.parseInt(args.get(1));
                  try
                  {
                        Document doc = Jsoup.connect(baseUrl)
                                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                                .referrer("http://www.google.com")
                                .get();

                        String toSend = String.format("http://%s - %s - %s", doc.getElementsByClass("link-text").get(num).text(), doc.select("a[href]").get(num).text(), doc.getElementsByClass("result-snippet").get(num).text());

                        event.sendMessage(toSend);
                        CommandCooldownHandler.addCooldown(event.getMember(), this);

                  }
                  catch (Exception e)
                  {
                        event.sendMessage("No results found for `" + args.get(0) + "` for search result `#" + num + "`");
                  }
            }
      }
}
