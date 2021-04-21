package schoolbot.handlers;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MessageHandler
{
    private final String[] FILE_EXTENSIONS = {"txt", "java", "cpp", "xml", "csharp", "asm", "js", "php", "r", "py", "go", "python", "ts"};
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


        if (event.getMessage().getContentRaw().startsWith("Hi Apples!"))
        {
            event.getChannel().sendMessage("Hi! Tell me your name, or say \"Stop\"!").queue();
        }


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

        for (Message.Attachment attachment : attachments)
        {
            boolean hasFileExt = false;
            for (String fileExt : FILE_EXTENSIONS)
            {
                if (Objects.equals(attachment.getFileExtension(), fileExt))
                {
                    hasFileExt = true;
                    break;
                }
            }

            if (hasFileExt)
            {
                event.getChannel().sendMessage("Uploading to pastecord....").queue(futureMessage ->
                {
                    final String uniqueIdentifier = UUID.randomUUID().toString() + author.getId();
                    CompletableFuture<File> future = attachment.downloadToFile()
                            .whenCompleteAsync((file, throwable) ->
                            {
                                if (throwable != null)
                                {
                                    LOGGER.error("Error occurred:", throwable);
                                    futureMessage.editMessage("Upload to pastecord failed!").queue();
                                }
                                else
                                {
                                    String urlToSend = "https://pastecord.com/" + sendPost(file);
                                    futureMessage.editMessage("File uploaded for " + event.getAuthor().getAsMention() + " " + urlToSend).queue();
                                }
                            });
                    event.getMessage().delete().queue();
                });
            }
        }
    }


    private String sendPost(File file)
    {

        RequestBody body = RequestBody.create(file,
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://pastecord.com/documents")
                .addHeader("User-Agent", "School bot (https://github.com/tykoooo/School-Bot-Remastered)")
                .post(body)
                .build();

        String url = "";
        try
        {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
            {
                LOGGER.error("Request not successful. Check pastecord.com");
            }
            else
            {
                url = response.body().string().split("\"")[3];
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error occurred: {}", e.getCause(), e);
        }
        return url;
    }

}
