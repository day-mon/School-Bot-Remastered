package schoolbot.commands.school.pitt;

import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Checks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseLookUp extends Command
{
    private static final String CLASS_SEARCH_URL = "https://psmobile.pitt.edu/app/catalog/classSearch";
    private static final String[] CAMPUSES = {"UPJ", "UPB", "PITT", "UPG", "UPT"};
    private OkHttpClient client;

    public CourseLookUp()
    {
        super("Looks up a course given a course number", "", 0);
        addCalls("clookup");
        addCooldown(5000L);
        client = new OkHttpClient();

    }

    @Override
    public void run(CommandEvent event)
    {
        if (Checks.isNumber(event.getArgs().get(0)))
        {
            return;
        }

        try
        {
            try
            {
                RequestBody formBody = new FormBody.Builder()
                        .add("CSRFToken", "7662cbcb0942dbbe2263277ea91c05ce")
                        .add("term", "2214")
                        .add("campus", "UPJ")
                        .add("acad_carer", "UGRD")
                        .add("subject", event.getArgs().get(0))
                        .build();

                Request request = new Request.Builder()
                        .url(CLASS_SEARCH_URL)
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36")
                        .post(formBody)
                        .build();

                Response reponse = client.newCall(request).execute();
                if (!reponse.isSuccessful())
                {
                    event.sendMessage("pwned");
                }
                else
                {
                    System.out.println(reponse.body().string());
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {

        }
    }

    private String getSubjectJson()
    {
        try
        {
            String CLASS_SEARCH_URL = "https://psmobile.pitt.edu/app/catalog/classSearch";
            OkHttpClient client = new OkHttpClient();


            Document doc = Jsoup.connect(CLASS_SEARCH_URL)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .get();


            Pattern pat = Pattern.compile("(?=subjects\\s*:\\s).*,");
            Matcher matcher = pat.matcher(doc.outerHtml());

            String matches = "";
            while (matcher.find())
            {
                matches += matcher.group();
            }
            matches = matches.substring(26, matches.length() - 1);
            return matches;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
