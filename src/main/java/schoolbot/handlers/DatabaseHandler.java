package schoolbot.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.config.ConfigOption;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseHandler
{
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private Connection dbConnection;
      private final ConfigHandler configHandler;
      private final Schoolbot schoolbot;

      public DatabaseHandler(Schoolbot schoolbot)
      {
            this.configHandler = schoolbot.getConfigHandler();
            this.schoolbot = schoolbot;
            initConnection();
      }

      public void initConnection()
      {
            try
            {
                  dbConnection = DriverManager.getConnection(
                          configHandler.getString(ConfigOption.JDBCURL) +
                          configHandler.getString(ConfigOption.DBHOSTNAME),
                          configHandler.getString(ConfigOption.DBUSER),
                          configHandler.getString(ConfigOption.DBPASSWORD));
            }
            catch (Exception e)
            {
                  LOGGER.error("Database could not connect correctly", e);
                  System.exit(1);
            }
      }


      public Connection getDbConnection()
      {
            initConnection();
            return dbConnection;
      }

      public void close()
      {
            try
            {
                  dbConnection.close();
                  LOGGER.info("Successfully closed database connection");
            }
            catch (Exception e)
            {
                  LOGGER.error("Could not close database", e);
            }
      }
}




