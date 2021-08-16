package schoolbot.commands.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SchoolAdd extends Command
{
      private static final String API_URL = "https://schoolapi.schoolbot.dev/search?name=";
      private static final String BACKUP_API_URL = "http://universities.hipolabs.com/search?name=";

      public SchoolAdd(Command parent)
      {
            super(parent, "Adds a school to the server", "[school name]", 1);
            addUsageExample("school add 'University of Pittsburgh'");
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL);
            addFlags(CommandFlag.DATABASE);

      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var firstArg = args.get(0);
            var channel = event.getChannel();

            Document doc;
            try
            {
                  doc = Jsoup.connect(API_URL + firstArg)
                          .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                          .referrer("https://www.google.com")
                          .ignoreContentType(true)
                          .get();
            }
            catch (IOException e)
            {
                  EmbedUtils.error(event, "Could not connect to the " + StringUtils.hyperText("School API", API_URL));
                  return;
            }


            String parseAbleJson =
                    Jsoup.parse(doc.outerHtml())
                            .body()
                            .text();

            JSONArray json;

            try
            {
                  json = new JSONArray(parseAbleJson);
            }
            catch (JSONException e)
            {
                  EmbedUtils.error(event, "Error occurred while parsing JSON");
                  return;
            }


            if (json.length() > 40)
            {
                  EmbedUtils.error(event, "Your search for ```" + firstArg + "``` contained ***" + json.length() + "*** elements.. Please narrow down your search");
                  return;
            }

            Map<Integer, MessageEmbed> schools = evalSchools(json);

            if (schools.isEmpty())
            {
                  EmbedUtils.error(event, "No schools matching " + firstArg + " found");
            }
            else if (schools.size() == 1)
            {
                  MessageEmbed embed = schools.get(1);

                  if (addToDbAndCreateRole(embed, event))
                  {
                        channel.sendMessage("School Created")
                                .setEmbeds(embed)
                                .queue();
                  }
                  else
                  {
                        EmbedUtils.error(event, " ** %s ** already exist", firstArg);
                  }
            }
            else
            {
                  event.getMessage().reply("More than one school that has been found with your search.. Type the number that matches your desired result").queue();
                  event.bPaginator(List.copyOf(schools.values()));

                  var eventWaiter = event.getSchoolbot().getEventWaiter();


                  eventWaiter.waitForEvent(MessageReceivedEvent.class, messageEvent ->
                          {
                                if (messageEvent.getAuthor().getIdLong() != event.getMember().getIdLong()) return false;
                                if (messageEvent.getChannel().getIdLong() != event.getChannel().getIdLong()) return false;
                                var message = messageEvent.getMessage().getContentRaw();
                                if (!Checks.isNumber(message)) return false;

                                var number = Integer.parseInt(message);
                                var maxInclusive = schools.values().size();

                                if (!Checks.between(number, maxInclusive))
                                {
                                      EmbedUtils.error(event, "%d is not between 1-%d", number, maxInclusive);
                                      return false;
                                }
                                return true;
                          },

                          onActionEvent ->
                          {
                                var schoolChoice = Integer.parseInt(onActionEvent.getMessage().getContentRaw());
                                var embed = schools.get(schoolChoice);

                                if (addToDbAndCreateRole(embed, event))
                                {
                                      event.getChannel().sendMessageEmbeds(embed)
                                              .append("School Created")
                                              .queue();
                                }
                                else
                                {
                                      EmbedUtils.error(event, "School already exist");
                                }
                          }, 60, TimeUnit.SECONDS, () ->
                                  EmbedUtils.warn(event, "Command timed out after 60 seconds!"));


            }
      }

      private static boolean addToDbAndCreateRole(MessageEmbed embed, CommandEvent event)
      {
            String schoolName = embed.getTitle();

            if (event.schoolExist(schoolName)) return false;

            String url = embed.getFields().get(0).getValue();
            String suffix = embed.getFields().get(1).getValue();
            var guild = event.getGuild();


            guild.createRole()
                    .setName(schoolName)
                    .setColor(new Random().nextInt(0xFFFFFF))
                    .queue(role ->
                            event.addSchool(new School(
                                    schoolName,
                                    suffix,
                                    role.getIdLong(),
                                    guild.getIdLong(),
                                    url.contains(",") ? url.split(",")[0] : url
                            )));
            return true;
      }

      private Map<Integer, MessageEmbed> evalSchools(JSONArray jsonArray)
      {
            HashMap<Integer, MessageEmbed> em = new HashMap<>();
            var length = jsonArray.length();

            for (int i = 0; i < length; i++)
            {
                  JSONObject elementsWithinArray = jsonArray.getJSONObject(i);

                  String schoolName = elementsWithinArray.getString("name");
                  String country = elementsWithinArray.getString("country");

                  JSONArray webPagesArray = elementsWithinArray.getJSONArray("web_pages");
                  StringBuilder webPagesUrls = new StringBuilder();

                  var webPagesLength = webPagesArray.length();

                  for (int j = 0; j < webPagesLength; j++)
                  {
                        webPagesUrls.append(webPagesArray.getString(j)).append("\n");
                  }
                  JSONArray schoolDomainsElements = elementsWithinArray.getJSONArray("domains");
                  StringBuilder schoolDomains = new StringBuilder();

                  var schoolDomainsLength = schoolDomainsElements.length();

                  for (int k = 0; k < schoolDomainsLength; k++)
                  {

                        String schoolDomain = schoolDomainsElements.getString(k);
                        if (schoolDomain.contains("edu"))
                        {
                              String[] splitDomain = schoolDomain.split("\\.");
                              var splitDomainLength = splitDomain.length;

                              for (int s = 0; s < splitDomainLength; s++)
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
                          .setColor(Constants.DEFAULT_EMBED_COLOR)
                          .setFooter(String.format("Page %d/%d", i + 1, length))
                          .setTimestamp(Instant.now())
                          .build()
                  );
            }

            return em;
      }
}

