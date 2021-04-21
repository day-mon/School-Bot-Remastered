package schoolbot.natives.util;

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
