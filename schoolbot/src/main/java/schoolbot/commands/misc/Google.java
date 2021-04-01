package schoolbot.commands.misc;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import schoolbot.handlers.CommandCooldownHandler;
import schoolbot.handlers.CommandHandler;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.info.SystemInfo;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.Embed;

public class Google extends Command
{
    public Google()
    {
        super("Looks up google query", "[query]", 1);
        addCalls("g");
    }

    @Override
    public void run(CommandEvent event)
    {
        String baseUrl = "https://duckduckgo.com/?q=";
        String replacedStuff = event.getArgs().get(0).replaceAll("\\s", "+");
        baseUrl += replacedStuff;

        if (event.getArgs().size() == 1)
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
                event.sendMessage("No results found for `" + event.getArgs().get(0) + "`");
            }
        }
        else
        {
            if (!Checks.isNumber(event.getArgs().get(1)))
            {
                Embed.error(event, "Not a number");
                return;
            }
            int num = Integer.parseInt(event.getArgs().get(1));
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
                event.sendMessage("No results found for `" + event.getArgs().get(0) + "`");
            }
        }
    }
}
