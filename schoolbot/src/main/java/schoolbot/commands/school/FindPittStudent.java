package schoolbot.commands.school;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(createPostBody("Damon ", "list"))
                    .uri(URI.create(URL))
                    .build();

            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            System.out.println(response);
        }
        catch (Exception e)
        {
            System.out.println("fop");
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
