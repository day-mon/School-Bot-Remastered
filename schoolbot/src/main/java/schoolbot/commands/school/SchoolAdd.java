package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SchoolAdd extends Command
{
    private final EventWaiter waiter;
    private static final String API_URL = "http://universities.hipolabs.com/search?name=";

    public SchoolAdd(Command parent, EventWaiter waiter)
    {
        super(parent, "Adds a school to the server", "[school name] [school email suffix] [school reference]", 1);
        this.waiter = waiter;
        addPermissions(Permission.ADMINISTRATOR);

    }


    @Override
    public void run(CommandEvent event)
    {

        MessageChannel channel = event.getChannel();
        User user = event.getUser();
        String firstArg = event.getArgs().get(0);

        if (Checks.isNumber(firstArg))
        {
            Embed.error(event, "Your school name cannot contain numbers!");
            return;
        }

        try
        {
            Document doc = Jsoup.connect(API_URL + firstArg)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .get();

            String parseAbleJson =
                    Jsoup.parse(doc.outerHtml())
                            .body()
                            .text();


            JSONArray json = new JSONArray(parseAbleJson);

            if (json.length() > 40)
            {
                Embed.error(event, "Your search for ```" + firstArg + "``` contained ***" + json.length() + "*** elements.. Please narrow down your search");
                return;
            }

            Map<Integer, MessageEmbed> schools = evalSchools(json);

            if (schools.isEmpty())
            {
                Embed.error(event, "No schools matching " + firstArg + " found");
            }
            else if (schools.size() == 1)
            {
                MessageEmbed embed = schools.get(1);
                addToDbAndCreateRole(event.getGuild(), embed, event.getSchoolbot());
                event.sendMessage("School Created");
                event.sendMessage(embed);
            }
            else
            {
                event.sendMessage("More than one school that has been found with your search.. Type the number that matches your desired result");
                ArrayList<Page> pages = new ArrayList<>();
                MessageBuilder mb = new MessageBuilder();
                for (MessageEmbed em : schools.values())
                {
                    mb.clear();
                    pages.add(new Page(PageType.EMBED, em));
                }

                event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success ->
                {
                    Pages.paginate(success, pages);
                });

                waiter.waitForEvent(MessageReceivedEvent.class,

                        e -> e.getAuthor().equals(event.getUser())
                                && e.getChannel().equals(event.getChannel())
                                && !e.getMessage().equals(event.getMessage())
                                && e.getMessage().getContentRaw().chars().allMatch(Character::isDigit)
                                && between(Integer.parseInt(e.getMessage().getContentRaw()), 1, schools.size()),
                        e ->
                        {
                            int schoolChoice = Integer.parseInt(e.getMessage().getContentRaw());
                            MessageEmbed embed = schools.get(schoolChoice);
                            if (addToDbAndCreateRole(event.getGuild(), embed, event.getSchoolbot()))
                            {
                                e.getChannel().sendMessage("School Created").queue();
                                e.getChannel().sendMessage(embed).queue();
                            }
                            else
                            {
                                Embed.error(event, "School already exist");
                            }
                        },
                        // if the user takes more than a minute, time out
                        1, TimeUnit.MINUTES, () -> event.sendMessage("Sorry, you took too long."));


            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    private Map<Integer, MessageEmbed> evalSchools(JSONArray jsonArray)
    {
        HashMap<Integer, MessageEmbed> em = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++)
        {

            JSONObject elementsWithinArray = jsonArray.getJSONObject(i);

            String schoolName = elementsWithinArray.getString("name");
            String country = elementsWithinArray.getString("country");

            JSONArray webPagesArray = elementsWithinArray.getJSONArray("web_pages");
            StringBuilder webPagesUrls = new StringBuilder();
            for (int j = 0; j < webPagesArray.length(); j++)
            {
                webPagesUrls.append(webPagesArray.getString(j)).append("\n");
            }
            JSONArray schoolDomainsElements = elementsWithinArray.getJSONArray("domains");
            StringBuilder schoolDomains = new StringBuilder();
            for (int k = 0; k < schoolDomainsElements.length(); k++)
            {

                String schoolDomain = schoolDomainsElements.getString(k);
                System.out.println(schoolName + " " + schoolDomain);
                if (schoolDomain.contains("edu"))
                {
                    String[] splitDomain = schoolDomain.split("\\.");
                    for (int s = 0; s < splitDomain.length; s++)
                    {
                        if (splitDomain[s].equals("edu"))
                        {
                            schoolDomains.append("@").append(splitDomain[s - 1]).append(".edu").append("\n");
                            break;
                        }

                    }
                }
            }


            em.put(i + 1, new EmbedBuilder()
                    .setTitle(schoolName)
                    .addField("Web pages", webPagesUrls.toString(), false)
                    .addField("Email Suffix", schoolDomains.toString(), false)
                    .addField("Country", country, false)
                    .addField("School #", String.valueOf(i + 1), false)
                    .setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR)
                    .setFooter("Generated using http://universities.hipolabs.com/")
                    .setTimestamp(Instant.now())
                    .build()
            );
        }

        return em;
    }

    private boolean addToDbAndCreateRole(Guild guild, MessageEmbed embed, Schoolbot schoolbot)
    {
        int R = new Random().nextInt(255);
        int G = new Random().nextInt(255);
        int B = new Random().nextInt(255);

        Color color = new Color(R, G, B);
        try
        {
            guild.createRole()
                    .setName(embed.getTitle())
                    .setColor(color)
                    .queue(role -> DatabaseUtil.addSchool(schoolbot, embed.getTitle(), embed.getFields().get(1).getValue(), role.getIdLong(), guild.getIdLong()));
        }
        catch (Exception e)
        {
            return false;
        }
        return true;

    }

    private boolean between(int i, int minValueInclusive, int maxValueInclusive)
    {
        return (i >= minValueInclusive && i <= maxValueInclusive);
    }


}

