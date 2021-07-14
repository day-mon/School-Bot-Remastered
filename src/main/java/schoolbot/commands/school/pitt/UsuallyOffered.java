package schoolbot.commands.school.pitt;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class UsuallyOffered extends Command
{

      public UsuallyOffered()
      {
            super("Gives Season when class is typically offered", "[class identifier]", 1);
            addCalls("uo", "usuallyoffered", "whenis");
            addUsageExample("whenis 'CS 0457'");
            addCommandPrerequisites("Must be a class within the University of Pittsburgh system");

      }

      /**
       * Command to check when classes are usually offered
       * URL: https://psmobile.pitt.edu/app/catalog/listCoursesBySubject/UPITT/<FIRST LETTER OF SUBJECT>/<SUBJECT>
       * Ex: https://psmobile.pitt.edu/app/catalog/listCoursesBySubject/UPITT/C/CS
       * <p>
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

            var REQUEST_URL = new StringBuilder("https://psmobile.pitt.edu/app/catalog/listCoursesBySubject/UPITT/");
            var firstArg = args.get(0);
            var syntaxCheck = firstArg.split("\\s+").length == 2;

            if (!syntaxCheck)
            {
                  EmbedUtils.error(event, "Invalid syntax \n Usage Example: " + getUsageExample());
                  return;
            }


            // [C]S 101
            var subject = firstArg.split("\\s+")[0].toUpperCase();
            var firstCharacter = subject.charAt(0);


            REQUEST_URL
                    .append(firstCharacter)
                    .append("/")
                    .append(subject);

            Document document;


            try
            {
                  document = Jsoup.connect(REQUEST_URL.toString())
                          .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                          .referrer("https://www.google.com")
                          .ignoreContentType(true)
                          .get();
            }
            catch (IOException e)
            {
                  EmbedUtils.error(event, "Could not connect to PeopleSoft ");
                  e.printStackTrace();
                  return;
            }


            var elements = document.getElementsByClass("section-content");
            List<MessageEmbed> embedList = new ArrayList<>();

            for (var ele : elements)
            {
                  var identifier = ele.getElementsByClass("strong section-body").text();

                  if (identifier.equalsIgnoreCase(firstArg))
                  {

                        var className = ele.getElementsByClass("section-body").get(1).text();
                        var offering = ele.getElementsByClass("section-body").get(2).text();

                        embedList.add(new EmbedBuilder()
                                .setTitle(className + " (" + identifier + ")")
                                .setDescription(offering)
                                .setColor(new Random().nextInt(0xFFFFF))
                                .build()
                        );
                  }
            }

            if (embedList.isEmpty())
            {
                  EmbedUtils.error(event, "No class found with the identifier '%s'", firstArg);
                  return;
            }

            event.sendAsPaginator(embedList);

      }
}
