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

import java.util.ArrayList;
import java.util.List;

public class DiningStatus extends Command
{
      public DiningStatus(Command parent)
      {
            super(parent, "Displays whats dinning settings are open", "none", 0);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var baseUrl = "https://api.dineoncampus.com/v1/locations/status?site_id=5eb2ff424198d433da74c3bd&platform=0";

            Document doc;

            try
            {
                  doc = Jsoup.connect(baseUrl)
                          .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                          .referrer("https://www.google.com")
                          .ignoreContentType(true)
                          .get();
            }
            catch (Exception e)
            {
                  EmbedUtils.error(event, "Error while connecting to Dinning API");
                  return;
            }

            var json = Jsoup.parse(doc.outerHtml())
                    .body()
                    .text();
            var jsonObject = new JSONObject(json).getJSONArray("locations");
            var embeds = new ArrayList<MessageEmbed>();

            var size = jsonObject.length() - 1;
            //this is really lazy

            var k = 0;

            for (var c = 0; c < size; c++)
            {
                  var element = jsonObject.getJSONObject(c);
                  var name = element.get("name").toString();
                  var status = element.getJSONObject("status");
                  var openOrNot = status.get("label").toString();



                  embeds.add(
                          new EmbedBuilder()
                                  .setTitle(name)
                                  .addField("Status", StringUtils.capitalize(openOrNot), false)
                                  .setFooter("Page " + ++k + "/" + (size))
                                  .build()
                  );
            }

            event.bPaginator(embeds);
      }
}
