package schoolbot.handlers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageHandler
{
      private final List<String> FILE_EXTENSIONS = List.of(
              "txt", "java", "cpp", "xml", "csharp", "asm",
              "js", "php", "r", "py", "go", "python", "ts", "html",
              "css", "scss"
      );
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final Schoolbot schoolbot;
      private final OkHttpClient client;

      public MessageHandler(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
            client = new OkHttpClient();
      }

      public void handle(GuildMessageReceivedEvent event)
      {
            String messageStr = event.getMessage().getContentRaw();
            User author = event.getAuthor();
            Message message = event.getMessage();


            if (event.getMessage().getAttachments().size() > 0)
            {
                  handleFile(event);
            }

            if (!messageStr.startsWith(SchoolbotConstants.DEFAULT_PREFIX))
            {
                  return;
            }


            schoolbot.getCommandHandler().handle(event);

      }

      private void handleFile(GuildMessageReceivedEvent event)
      {
            User author = event.getAuthor();
            Message message = event.getMessage();
            List<Message.Attachment> attachments = message.getAttachments();
            message.delete().queue();


            attachments.stream()
                    .filter(attachment -> FILE_EXTENSIONS.contains(attachment.getFileExtension()))
                    .map(future ->
                    {
                          CompletableFuture<Message> messageFuture = event.getChannel().sendMessage("Uploading to pastecord...").submit();
                          CompletableFuture<InputStream> inputStreamFuture = future.retrieveInputStream();
                          CompletableFuture<Void> allFutures = CompletableFuture.allOf(messageFuture, inputStreamFuture);
                          return new Triple(messageFuture, inputStreamFuture, allFutures);
                    })
                    .forEach(trip ->
                    {
                          trip.val3.whenCompleteAsync((empty, object) ->
                          {
                                var sentMessage = trip.val1.getNow(null);
                                var inputStream = trip.val2.getNow(null);


                                if (sentMessage == null || inputStream == null)
                                {
                                      event.getChannel().sendMessage("Could not upload to pastecord..").queue();
                                      return;
                                }

                                var urlToSend = "https://pastecord.com/" + sendPost(inputStream);
                                sentMessage.editMessageFormat("File uploaded for %s [%s] ", event.getAuthor().getAsMention(), urlToSend).queue();
                          });
                    });
      }


      private String sendPost(InputStream file)
      {
            String url = "";
            Response response = null;

            try
            {
                  RequestBody body = RequestBody.create(IOUtils.toString(file, StandardCharsets.UTF_8),
                          MediaType.parse("application/json"));

                  Request request = new Request.Builder()
                          .url("https://pastecord.com/documents")
                          .addHeader("User-Agent", "School bot (https://github.com/tykoooo/School-Bot-Remastered)")
                          .post(body)
                          .build();


                  response = client.newCall(request).execute();
                  if (!response.isSuccessful())
                  {
                        LOGGER.error("Request not successful. Refer to pastecord or check your connection to it");
                  }
                  else
                  {
                        url = response.body().string().split("\"")[3];
                  }
            }
            catch (Exception e)
            {
                  LOGGER.error("Error occurred in MessageHandler", e);
            }
            finally
            {
                  if (response != null)
                  {
                        response.close();
                  }
            }
            return url;
      }

      public record Triple(CompletableFuture<Message> val1, CompletableFuture<InputStream> val2,
                           CompletableFuture<Void> val3)
      {
      }

}
