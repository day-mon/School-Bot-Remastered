package schoolbot.natives.util;

public class Checks 
{
    public static boolean isNumber(String number)
    {
        return number.matches("-?\\d+(\\.\\d+)?");
    }   
}
