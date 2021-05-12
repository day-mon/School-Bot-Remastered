package schoolbot.commands.fun;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LetMeGoogleThat extends Command
{

      public LetMeGoogleThat()
      {
            super("", "", 1);
            addCalls("lmgtfy", "googlepls", "plsgoogle");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            final String BASE_URL = "https://www.letmegooglethat.com/?q=";
            String urlToSend = args.get(0).replaceAll("\\s+", "+");

            event.getChannel().sendMessage("Hold on... Let me google that for you....").queueAfter(3, TimeUnit.SECONDS, message ->
            {
                  message.editMessage(BASE_URL + urlToSend).queue();
            });
      }
}
