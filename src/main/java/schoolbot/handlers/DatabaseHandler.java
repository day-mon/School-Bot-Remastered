package schoolbot.handlers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.util.DatabaseUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class DatabaseHandler
{
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final ConfigHandler configHandler;
      private final HikariDataSource pool;

      public DatabaseHandler(Schoolbot schoolbot)
      {
            this.configHandler = schoolbot.getConfigHandler();
            this.pool = initHikari();
            initTables();
      }


      private HikariDataSource initHikari()
      {
            var config = configHandler.getConfig();
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setDriverClassName(config.databaseConfig().dbDriver());
            hikariConfig.setJdbcUrl(config.databaseConfig().jbdcUrl() + config.databaseConfig().dbHostName());
            hikariConfig.setUsername(config.databaseConfig().dbUser());
            hikariConfig.setPassword(config.databaseConfig().dbPassword());

            /*
                    The property controls the maximum size that the pool is allowed to reach, including both idle and in-use connections.
                    Basically this valueBeingChanged will determine the maximum number of actual connections to the database backend.
                    When the pool reaches this size, and no idle connections are available,
                    calls to getConnection() will block for up to connectionTimeout milliseconds before timing out.
             */
            hikariConfig.setMaximumPoolSize(20);
            /*
                    The property controls the minimum number of idle connections that HikariCP tries to maintain in the pool, including both idle and in-use connections.
                    If the idle connections dip below this valueBeingChanged, HikariCP will make a best effort to restore them quickly and efficiently.


             */
            hikariConfig.setMinimumIdle(10);
            /*
                     Set the maximum number of milliseconds that a client will wait for a connection from the pool.
                     If this time is exceeded without a connection becoming available, a SQLException will be thrown from DataSource.getConnection().
                     15 Seconds = 15000 ms
             */
            hikariConfig.setConnectionTimeout(15000);


            try
            {
                  return new HikariDataSource(hikariConfig);
            }
            catch (Exception e)
            {
                  LOGGER.error("Connection failure... Check PostgreSQL password/username/hostname", e);
                  System.exit(1);
                  return null;
            }
      }

      public void initTables()
      {
            try
            {
                  final var root = schoolbot.util.IOUtils.getJarFilesystem(DatabaseHandler.class).resolve("sql");

                  if (null == root)
                  {
                        LOGGER.error("SQL Folder inside resources does not exist.. Try placing a folder in resources named 'sql'");
                        System.exit(1);
                        return;
                  }

                  final var sqlPaths = Files.walk(root)
                          .filter(path -> path.getFileName().toString().endsWith(".sql"))
                          .sorted()
                          .toList();


                  if (sqlPaths.isEmpty())
                  {
                        LOGGER.error("There are no SQL Files...");
                        return;
                  }

                  for (var file : sqlPaths)
                  {
                        String fileName = file.getFileName().toString();

                        var sqlTable = DatabaseUtils.class.getResourceAsStream("/sql/" + fileName);

                        if (sqlTable != null)
                        {
                              getDbConnection().createStatement().execute(IOUtils.toString(sqlTable, StandardCharsets.UTF_8));
                        }
                  }
            }
            catch (Exception e)
            {
                  LOGGER.error("Error Initializing Database Files", e);
            }
      }


      public Connection getDbConnection()
      {
            try
            {
                  return pool.getConnection();
            }
            catch (SQLException throwable)
            {
                  LOGGER.error("Database Error has occurred", throwable);
                  return null;
            }
      }

      public void close()
      {
            try
            {
                  pool.close();
                  LOGGER.info("Successfully closed database connection");
            }
            catch (Exception e)
            {
                  LOGGER.error("Could not close database", e);
            }
      }

      public HikariDataSource getPool()
      {
            return pool;
      }

}




