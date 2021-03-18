package schoolbot.commands.misc;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

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


        try
        {
            Document doc = Jsoup.connect(baseUrl)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            System.out.println(doc.text());
            System.out.println(doc.getElementById("web_content_wrapper").text());

        }
        catch (Exception e)
        {
            System.out.println("Retard");
        }
    }
}
