package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.List;

public class Wolfram extends Command
{

    public Wolfram()
    {
        super("Allows a user to search for math problems on wolframalpha.com", "[problem]", 1);
        addCalls("wolf", "wolfram");
    }

    @Override
    public void run(@NotNull CommandEvent event, @NotNull List<String> args)
    {
        String finalSend = args.get(0).replaceAll("%", "%25").replaceAll("\\+", "%2B").replaceAll("\\s", "+")
                .replaceAll("\\$", "%20").replaceAll("&", "%26").replaceAll("\\^", "%5E").replaceAll("`", "%60")
                .replaceAll("/", "%2F").replaceAll("'", "%27").replaceAll("\\{", "%7B").replaceAll("}", "%7D")
                .replaceAll(",", "%2C").replaceAll("\"", "%22").replaceAll("[“]", "%22").replaceAll("[”]", "%22")
                .replaceAll("\\|", "%7C").replaceAll("=", "%3D").replaceAll(":", "%3A").replaceAll("<", "%3C")
                .replaceAll("\\(", "%28").replaceAll("\\)", "%29").replaceAll(">", "%3E").replaceAll(";", "%3B")
                .replaceAll("~", "%7E").replaceAll("@", "%40");

        event.sendMessage("https://www.wolframalpha.com/input/?i=" + finalSend);

    }
}
