package schoolbot.commands.school.pitt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.school.LaundryModel;
import schoolbot.util.EmbedUtils;


import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Laundry extends Command
{
      private static final String BASE_URL = "https://johnstown.schoolbot.dev/api/laundry/";


      public Laundry()
      {
            super("Displays all laundry machine in a given a housing name", "[housing name]", 1);
            addCalls("laundry");
            addFlags(CommandFlag.INTERNET);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var potLaundryName = args.get(0).toUpperCase();
            var laundryURL = BASE_URL + potLaundryName;

            Document doc;
            try
            {
                  doc = Jsoup.connect(laundryURL)
                          .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                          .referrer("https://www.google.com")
                          .ignoreContentType(true)
                          .get();
            }
            catch (Exception e)
            {
                  event.sendMessage("Cannot connect to laundry api... exiting");
                  return;
            }

            var om = new ObjectMapper();
            List<LaundryModel> models;
            try
            {
                  models = om.readValue(Jsoup.parse(doc.outerHtml()).body().text(), new TypeReference<>() { });
            }
            catch (JsonProcessingException e)
            {
                  event.sendMessage("API provided a bad response");
                  event.getCommand().getLOGGER().error("Error has occurred parsing JSON. \n Stack Trace: {}", e.getMessage());
                  return;
            }

            if (models.isEmpty())
            {
                  event.sendMessage("%s is not a valid laundry name", potLaundryName);
                  return;
            }


            var embeds = new ArrayList<MessageEmbed>();


            for (var model : models)
            {
                  embeds.add(
                          new EmbedBuilder()
                                  .setTitle(String.format("Appliance ID [#%s]", model.applianceID.replaceAll("0", "")))
                                  .addField("Appliance Type", model.type, false)
                                  .addField("Working", model.isWorking ? "Yes" : "No" , false)
                                  .addField(model.isInUse ? "Time Remaining" : "In use",  model.isInUse ? model.timeRemaining : "No", false)
                                  .setColor(model.isWorking ? Color.green : Color.red)
                                  .build()
                  );
            }

            event.bPaginator(embeds);


      }
}

