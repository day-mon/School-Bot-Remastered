package schoolbot.commands.school;

import org.apache.tools.ant.taskdefs.Echo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class LeetCode extends Command
{
    public LeetCode()
    {
        super("", "", 0);
        addCalls("l");
    }

    @Override
    public void run(CommandEvent event)
    {
        System.out.println("s");
        final String baseUrl = "https://leetcode.com/problemset/all/";
        try
        {
            Document doc = Jsoup.connect(baseUrl)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            System.out.println(doc.outerHtml());
        }
        catch (Exception e)
        {
            System.out.println("lol");
        }

    }
}
