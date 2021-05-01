package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SchoolAdd extends Command
{
    private static final String API_URL = "http://universities.hipolabs.com/search?name=";

      public SchoolAdd(Command parent)
      {
            super(parent, "Adds a school to the server", "[school name] [school email suffix] [school reference]", 1);
            addPermissions(Permission.ADMINISTRATOR);
            addFlags(CommandFlag.DATABASE);

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
                  for (MessageEmbed embeds : schools.values())
                  {
                        pages.add(new Page(PageType.EMBED, embeds));
                  }
                  event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success ->
                          Pages.paginate(success, pages));

                  event.getJDA().addEventListener(new SchoolStateMachine(event.getSchoolbot(), event.getChannel(), event.getUser(), schools));
            }
        }
        catch (Exception e)
        {
              e.printStackTrace();
        }
    }


      public static class SchoolStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private final Map<Integer, MessageEmbed> schools;
            private final Schoolbot schoolbot;

            private int state = 0;


            public SchoolStateMachine(Schoolbot schoolbot, MessageChannel channel, User author, Map<Integer, MessageEmbed> schools)
            {
                  this.authorID = author.getIdLong();
                  this.channelID = channel.getIdLong();
                  this.schools = schools;
                  this.schoolbot = schoolbot;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().getIdLong() != authorID) return;
                  if (event.getChannel().getIdLong() != channelID) return;
                  if (!event.getMessage().getContentRaw().chars().allMatch(Character::isDigit)) return;
                  if (!between(Integer.parseInt(event.getMessage().getContentRaw()), 1, schools.size())) return;

                  int schoolChoice = Integer.parseInt(event.getMessage().getContentRaw());
                  MessageEmbed embed = schools.get(schoolChoice);


                  if (addToDbAndCreateRole(event.getGuild(), embed, schoolbot))
                  {
                        event.getChannel().sendMessage("School Created").queue();
                        event.getChannel().sendMessage(embed).queue();
                  }
                  else
                  {
                        Embed.error(event, "School already exist");
                  }

                  event.getJDA().removeEventListener(this);
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

      private static boolean addToDbAndCreateRole(Guild guild, MessageEmbed embed, Schoolbot schoolbot)
      {
            String schoolName = embed.getTitle();
            School school = DatabaseUtil.getSpecificSchoolBySchoolName(schoolbot, schoolName, guild.getIdLong());

            if (school != null)
            {
                  return false;
            }


            guild.createRole()
                    .setName(embed.getTitle())
                    .setColor(new Random().nextInt(0xFFFFFF))
                    .queue(role ->
                    {
                          DatabaseUtil.addSchool(schoolbot,
                                  embed.getTitle(),
                                  embed.getFields().get(1).getValue(),
                                  role.getIdLong(),
                                  guild.getIdLong());
                    });
            return true;
      }


      private static boolean between(int i, int minValueInclusive, int maxValueInclusive)
      {
            return (i >= minValueInclusive && i <= maxValueInclusive);
      }


}

