package schoolbot.handlers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
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
            var author = event.getAuthor();
            var channel = event.getChannel();
            var message = event.getMessage();
            var guild = event.getGuild();
            var prefix = schoolbot.getWrapperHandler().fetchGuildPrefix(guild.getIdLong());
            var selfUser = event.getJDA().getSelfUser();

            if (message.getAttachments().size() > 0)
            {
                  handleFile(event);
                  return;
            }

            if (isBotMention(event))
            {
                  channel.sendMessage("Guild prefix: " + prefix).queue();
                  return;
            }

            if (!messageStr.startsWith(prefix))
            {
                  return;
            }

            schoolbot.getCommandHandler().handle(event, prefix);

      }

      private void handleFile(GuildMessageReceivedEvent event)
      {
            User author = event.getAuthor();
            Message message = event.getMessage();
            List<Message.Attachment> attachments = message.getAttachments();


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
                                  message.delete().queue();

                            }));
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

                  LocalDateTime ldt = LocalDateTime.now();
                  response = client.newCall(request).execute();
                  LOGGER.debug("Response time to execute: {} ms", Duration.between(ldt, LocalDateTime.now()).toMillis());

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

      public boolean isBotMention(GuildMessageReceivedEvent event)
      {
            var message = event.getMessage().getContentRaw();
            var selfUser = event.getJDA().getSelfUser();
            return message.startsWith("<@" + selfUser.getIdLong() + ">") || message.startsWith("<@!" + selfUser.getIdLong() + ">");
      }


      public record Triple(CompletableFuture<Message> val1, CompletableFuture<InputStream> val2,
                           CompletableFuture<Void> val3)
      {
      }

}
