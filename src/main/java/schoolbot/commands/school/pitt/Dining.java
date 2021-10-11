package schoolbot.commands.school.pitt;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Dining extends Command
{
      public Dining()
      {
            super("Tells students whats for breakfast/lunch/dinner", "[time period]", 1);
            addUsageExample("dinning lunch");
            addCalls("dining");
            addChildren(
                    new DiningStatus(this)
            );
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var arg = args.get(0);

            if (!(arg.equalsIgnoreCase("breakfast") || arg.equalsIgnoreCase("lunch") || arg.equalsIgnoreCase("dinner")))
            {
                  EmbedUtils.error(event, "%s is not a valid period, please choose a correct time period **breakfast**, **lunch** or **dinner**", arg);
                  return;
            }

            var period = getPeriod(event, arg);

            if (period.equals(""))
            {
                  EmbedUtils.error(event, "Could not find period");
                  return;
            }

            event.sendSelfDeletingMessage("This may take a while to sit back and relax");

            var baseUrl = "https://api.dineoncampus.com/v1/location/5f3c3313a38afc0ed9478518/periods/%s?platform=0&date=".formatted(period);


            var embeds = getEmbeds(event, baseUrl);

            if (embeds == null)
            {
                  return;
            }

            event.bPaginator(embeds);

      }


      /**
       * @param event  CommandEvent
       * @param period String value of period (i.e Breakfast, Dinner, Lunch)
       * @return Can return empty string if period isnt found else returns period id
       */
      private String getPeriod(CommandEvent event, String period)
      {
            var requestUrl = "https://api.dineoncampus.com/v1/location/5f3c3313a38afc0ed9478518/periods?platform=0&date=" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());

            Document doc;

            try
            {
                  doc = Jsoup.connect(requestUrl)
                          .referrer("https://www.google.com")
                          .ignoreContentType(true)
                          .get();
            }
            catch (Exception e)
            {
                  EmbedUtils.error(event, "Error while connecting to Dinning API");
                  e.printStackTrace();
                  return "";
            }
            var json = Jsoup.parse(doc.outerHtml())
                    .body()
                    .text();

            var jsonArr = new JSONObject(json)
                    .getJSONArray("periods");

            for (var k = 0; k < jsonArr.length(); k++)
            {
                  var jsonObject = jsonArr.getJSONObject(k);
                  var name = jsonObject.getString("name");
                  var id = jsonObject.getString("id");

                  if (name.equalsIgnoreCase(period))
                  {
                        return id;
                  }

            }
            return "";

      }


      private List<MessageEmbed> getEmbeds(CommandEvent event, String url)
      {
            var list = new ArrayList<MessageEmbed>();

            url += DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());

            Document doc;

            try
            {
                  doc = Jsoup.connect(url)
                          .referrer("https://www.google.com")
                          .ignoreContentType(true)
                          .get();
            }
            catch (Exception e)
            {
                  EmbedUtils.error(event, "Error while connecting to Dinning API");
                  e.printStackTrace();
                  return null;
            }
            var json = Jsoup.parse(doc.outerHtml())
                    .body()
                    .text();


            var jsonArray = new JSONObject(json)
                    .getJSONObject("menu")
                    .getJSONObject("periods")
                    .getJSONArray("categories");


            var c = 0;

            for (var k = 0; k < jsonArray.length(); k++)
            {
                  var jsonArr = jsonArray.getJSONObject(k);
                  var g = jsonArr.getJSONArray("items");
                  var whereFrom = jsonArr.getString("name");

                  for (var l = 0; l < g.length(); l++)
                  {
                        var ks = g.getJSONObject(l);
                        var name = ks.getString("name");
                        var ingredients = ks.getString("ingredients");

                        list.add(new EmbedBuilder()
                                .setTitle("Whats for " + StringUtils.capitalize(event.getArgs().get(0)) + " Today")
                                .addField("Name", name, false)
                                .addField("Ingredients", ingredients, false)
                                .addField("From", whereFrom, true)
                                .setFooter("Page " + ++c + " of many")
                                .build());
                  }
            }

            return list;

      }
}
