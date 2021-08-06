package schoolbot.handlers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.util.EmbedUtils;

import java.io.IOException;
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
      private final OkHttpClient client = new OkHttpClient();
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final Schoolbot schoolbot;


      public MessageHandler(Schoolbot schoolbot)
      {
            this.schoolbot = schoolbot;
      }

      public void handle(GuildMessageReceivedEvent event)
      {
            String messageStr = event.getMessage().getContentRaw();
            var channel = event.getChannel();
            var message = event.getMessage();
            var guild = event.getGuild();
            var prefix = schoolbot.getWrapperHandler().fetchGuildPrefix(guild.getIdLong());

            if (!message.getAttachments().isEmpty())
            {
                  handleFile(event);
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
            var message = event.getMessage();
            List<Message.Attachment> attachments = message.getAttachments();

            attachments.stream()
                    .filter(attachment -> FILE_EXTENSIONS.contains(attachment.getFileExtension()))
                    .map(future ->
                    {
                          var messageFuture = event.getChannel().sendMessage("Uploading to pastecord...").submit();
                          var inputStreamFuture = future.retrieveInputStream();
                          var allFutures = CompletableFuture.allOf(messageFuture, inputStreamFuture);
                          return new Triple(messageFuture, inputStreamFuture, allFutures);
                    })
                    .forEach(trip ->
                            trip.val3.whenComplete((empty, throwable) ->
                            {
                                  if (throwable != null)
                                  {
                                        LOGGER.error("Error occurred whilst uploading to pastecord", throwable);
                                        EmbedUtils.error(event, "Error occurred whilst uploading to pastecord");
                                        return;
                                  }

                                  doUpload(trip, event);

                            }));
      }


      private void doUpload(Triple trip, GuildMessageReceivedEvent event)
      {
            Request request;

            var sentMessage = trip.val1.getNow(null);
            var file = trip.val2.getNow(null);
            var message = event.getMessage();

            try
            {
                  request = new Request.Builder()
                          .url("https://pastecord.com/documents")
                          .addHeader("User-Agent", "School bot (https://github.com/tykoooo/School-Bot-Remastered)")
                          .post(RequestBody.create(IOUtils.toString(file, StandardCharsets.UTF_8),
                                  MediaType.parse("application/json")))
                          .build();

                  file.close();
            }
            catch (Exception e)
            {
                  LOGGER.error("Error occurred while parsing input stream", e);
                  return;
            }

            client.newCall(request).enqueue(
                    new Callback()
                    {
                          @Override
                          public void onFailure(@NotNull Call call, @NotNull IOException e)
                          {
                                sentMessage.editMessage("Error occurred while uploading to pastecord").queue();
                                LOGGER.error("Error upon sending request!", e);
                          }

                          @Override
                          public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                          {

                                if (!response.isSuccessful())
                                {
                                      LOGGER.error("Request was not successful. Returned a {} error", response.code());
                                      return;
                                }

                                var pastecordEnding = response.body().string().split("\"")[3];
                                var urlToSend = "https://pastecord.com/" + pastecordEnding;

                                sentMessage.editMessageFormat("File uploaded for %s [%s] ", event.getAuthor().getAsMention(), urlToSend).queue();
                                message.delete().queue();
                          }
                    }
            );
      }

      public boolean isBotMention(GuildMessageReceivedEvent event)
      {
            var message = event.getMessage().getContentRaw();
            var selfUser = event.getJDA().getSelfUser();
            return message.startsWith("<@" + selfUser.getIdLong() + ">") || message.startsWith("<@!" + selfUser.getIdLong() + ">");
      }


      private record Triple(CompletableFuture<Message> val1, CompletableFuture<InputStream> val2,
                            CompletableFuture<Void> val3)
      {
      }

}
