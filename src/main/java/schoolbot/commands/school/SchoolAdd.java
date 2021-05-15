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
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.time.Instant;
import java.util.*;

public class SchoolAdd extends Command
{
      // This can be replaced with http://universities.hipolabs.com/ if the ip below is not available
      private static final String API_URL = "http://193.239.146.56:5000/search?name=";
      private static final String BACKUP_API_URL = "http://universities.hipolabs.com/search?name=";

      public SchoolAdd(Command parent)
      {
            super(parent, "Adds a school to the server", "[school name] [school email suffix] [school reference]", 1);
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES);
            addFlags(CommandFlag.DATABASE);

      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            MessageChannel channel = event.getChannel();
            User user = event.getUser();
            String firstArg = args.get(0);
            Guild guild = event.getGuild();
            Schoolbot schoolbot = event.getSchoolbot();

            if (Checks.isNumber(firstArg))
            {
                  Embed.error(event, "Your school name cannot contain numbers!");
                  return;
            }
            Document doc = null;
            try
            {
                  doc = Jsoup.connect(BACKUP_API_URL + firstArg)
                          .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                          .referrer("http://www.google.com")
                          .ignoreContentType(true)
                          .get();
            }
            catch (Exception e)
            {
                  channel.sendMessage("Cannot connect to API... exiting..").queue();
                  return;
            }


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
                  if (addToDbAndCreateRole(embed, schoolbot, event))
                  {
                        event.sendMessage("School Created");
                        event.sendMessage(embed);
                  }
                  else
                  {
                        Embed.error(event, " ** %s ** already exist", firstArg);
                  }
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


                  //TODO: Why would I ever do this if I pass the event through.. Fix this later..
                  event.getJDA().addEventListener(new SchoolStateMachine(event.getSchoolbot(), event.getChannel(), event.getUser(), schools, event));
            }
      }


      public static class SchoolStateMachine extends ListenerAdapter
      { // start
            private final long channelID, authorID;
            private final Map<Integer, MessageEmbed> schools;
            private final Schoolbot schoolbot;
            private final CommandEvent cmdEvent;

            private final int state = 0;


            public SchoolStateMachine(Schoolbot schoolbot, MessageChannel channel, User author, Map<Integer, MessageEmbed> schools, CommandEvent cmdEvent)
            {
                  this.cmdEvent = cmdEvent;
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
                  if (!Checks.between(Integer.parseInt(event.getMessage().getContentRaw()), 1, schools.size())) return;

                  int schoolChoice = Integer.parseInt(event.getMessage().getContentRaw());
                  MessageEmbed embed = schools.get(schoolChoice);
                  CommandEvent commandEvent = cmdEvent;
                  Guild guild = event.getGuild();

                  if (event.getMessage().getContentRaw().equalsIgnoreCase("stop"))
                  {
                        event.getChannel().sendMessage("Okay aborting..").queue();
                        event.getJDA().removeEventListener(this);
                        return;
                  }


                  if (addToDbAndCreateRole(embed, schoolbot, commandEvent))
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
      } // end


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
                          .setFooter("Big thanks to https://github.com/Hipo/university-domains-list-api")
                          .setTimestamp(Instant.now())
                          .build()
                  );
            }

            return em;
      }

      private static boolean addToDbAndCreateRole(MessageEmbed embed, Schoolbot schoolbot, CommandEvent event)
      {
            String schoolName = embed.getTitle();

            if (event.schoolExist(schoolName)) return false;

            String url = embed.getFields().get(0).getValue();
            String suffix = embed.getFields().get(1).getValue();
            Guild guild = event.getGuild();


            guild.createRole()
                    .setName(schoolName)
                    .setColor(new Random().nextInt(0xFFFFFF))
                    .queue(role ->
                    {
                          event.addSchool(event, new School(
                                  schoolName,
                                  suffix,
                                  role.getIdLong(),
                                  guild.getIdLong(),
                                  url.contains(",") ? url.split(",")[0] : url
                          ));
                    });
            return true;
      }
}

