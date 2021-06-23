package schoolbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Date;


public class Main
{
      public static void main(String[] args)
      {
            Schoolbot bot = new Schoolbot();
            final Logger MAIN_LOGGER = LoggerFactory.getLogger(Main.class);

            try
            {
                  bot.build();
                  MAIN_LOGGER.info("Bot built successfully @ " + new Date());
            }
            catch (LoginException e)
            {
                  MAIN_LOGGER.error("Token is invalid", e);
            }
      }
}