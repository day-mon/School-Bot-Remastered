package schoolbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Date;


public class Main
{
      public static void main(String[] args)
      {
            final Schoolbot bot = new Schoolbot();
            final Logger logger = LoggerFactory.getLogger(Main.class);

            try
            {
                  bot.build();
                  logger.info("Bot built successfully @ " + new Date());
            }
            catch (LoginException e)
            {
                  logger.error("Token is invalid", e);
            }
      }
}