package schoolbot.natives.util;

import net.dv8tion.jda.api.events.Event;
import schoolbot.natives.objects.command.CommandEvent;

public class Checks
{
    public static boolean isNumber(String number)
    {
        return number.matches("-?\\d+(\\.\\d+)?");
    }   

    public static boolean isValidEmail(String potentialEmail) {
        return potentialEmail.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.(?:[A-Z]{2}|com|org|edu|net|)$\n");
    }

}
