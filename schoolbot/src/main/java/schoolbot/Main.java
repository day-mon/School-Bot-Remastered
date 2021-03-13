package schoolbot;

import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] args) {
        Schoolbot bot = new Schoolbot();

        try 
        {
            bot.build();
        }
        catch (LoginException e)
        {
            System.out.println("Invalid Token");
        }
        catch (InterruptedException e)
        {
            System.out.println("Schoolbot was interrupted on start up. Please try again!");
        }
    }
    
}