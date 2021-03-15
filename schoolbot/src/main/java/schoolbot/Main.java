package schoolbot;

import java.util.Date;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import schoolbot.natives.objects.config.ConfigOption;


public class Main {
    public static void main(String[] args) {
        Schoolbot bot = new Schoolbot();
        final Logger MAIN_LOGGER = LoggerFactory.getLogger(Main.class);

        try 
        {
            bot.build();
            MAIN_LOGGER.info("Bot built successfully @ " + new Date());
        }
        catch (LoginException e)
        {
            bot.getLogger().error("Token is invalid");
        }
        catch (InterruptedException e)
        {
            bot.getLogger().debug("Schoolbot was interrupted on start up. Please try again!");
        }
    }
    
}