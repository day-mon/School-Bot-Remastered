package schoolbot.commands.school.pitt;


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
import schoolbot.util.Embed;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Laundry extends Command
{
      private final static String BASE_URL = "https://www.laundryview.com/api/currentRoomData?school_desc_key=4590&location=";
      private final static Map<String, String> LAUNDRY_API_CALLS = Map.ofEntries(
              Map.entry("HICKORY", "5813396"),
              Map.entry("BRIAR", "581339005"),
              Map.entry("BUCKHORN", "5813393"),
              Map.entry("LLC", "58133912"),
              Map.entry("OAK", "5813391"),
              Map.entry("HAWTHORN", "5813397"),
              Map.entry("HEATHER", "5813398"),
              Map.entry("HEMLOCK", "5813392"),
              Map.entry("MAPLE", "5813399"),
              Map.entry("WILLOW", "58133912"),
              Map.entry("LARKSPUR", "58133911"),
              Map.entry("LAUREL", "5813394"),
              Map.entry("CPAS", "581339013")
      );


      public Laundry()
      {
            super("Displays all laundry machine in a given a housing name", "[housing name]", 1);
            addCalls("laundry");
            addFlags(CommandFlag.INTERNET);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String potLaundryName = args.get(0).toUpperCase();

            if (!LAUNDRY_API_CALLS.containsKey(potLaundryName))
            {
                  Embed.error(event, "Housing doesnt exist");
                  return;
            }

            String laundryURL = BASE_URL + LAUNDRY_API_CALLS.get(potLaundryName);

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

            String parseAbleJson =
                    Jsoup.parse(doc.outerHtml())
                            .body()
                            .text();


            JSONObject jsonObject = new JSONObject(parseAbleJson);
            JSONArray jsonArray = jsonObject.getJSONArray("objects");
            ArrayList<MessageEmbed> embeds = new ArrayList<>();


            for (int i = 0; i < jsonArray.length(); i++)
            {
                  JSONObject ele = jsonArray.getJSONObject(i);
                  String typeString = ele.get("type").toString();
                  String applianceType = typeString.toUpperCase().startsWith("D") ? "Dryer" : "Washer";
                  if (typeString.toUpperCase().contains("DRY") || typeString.contains("washFL"))
                  {
                        String working = ele.getBigDecimal("percentage").compareTo(new BigDecimal("5.0")) <= 0 ? "Yes" : "No";


                        if (ele.getInt("status_toggle") > 0)
                        {
                              embeds.add(new EmbedBuilder()
                                      .setTitle("# " + i + " Appliance ID [" + ele.getString("appliance_desc") + "]")
                                      .addField("Appliance Type", applianceType, false)
                                      .addField("Working", working, false)
                                      .addField("Time Remaining", ele.getString("time_left_lite"), false)
                                      .setColor(Color.red)
                                      .build());
                        }
                        else
                        {
                              embeds.add(new EmbedBuilder()
                                      .setTitle("# " + i + " Appliance ID [" + ele.getString("appliance_desc") + "]")
                                      .addField("Appliance Type", applianceType, false)
                                      .addField("Working", working, false)
                                      .addField("In use", "No", false)
                                      .setColor(Color.green)
                                      .build());
                        }
                  }
            }


            event.sendAsPaginator(embeds);


      }
}

