package schoolbot.commands.school;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

public class FindPittStudent extends Command
{
    public FindPittStudent()
    {
        super(" ", " ", 1);
        addCalls("f");
    }

    @Override
    public void run(CommandEvent event)
    {
        final String URL = "https://find.pitt.edu/Search";

        try
        {

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("search", event.getArgs().get(0))
                    .add("layout", "list")
                    .build();

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("User-Agent", "School bot (https://github.com/tykoooo/School-Bot-Remastered)")
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful())
            {
                System.out.println(response.body().string());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static HttpRequest.BodyPublisher createPostBody(String searchName, String layout) throws JsonProcessingException
    {
        Map<String, String> postParams = new HashMap<>();
        postParams.put("search", searchName);
        postParams.put("layout", layout);

        ObjectMapper mapper = new ObjectMapper();
        return HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(postParams));
    }
}
