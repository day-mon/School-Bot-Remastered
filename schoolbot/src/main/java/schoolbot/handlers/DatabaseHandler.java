package schoolbot.handlers;

import schoolbot.Schoolbot;
import schoolbot.natives.objects.config.ConfigOption;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseHandler
{
    private final Schoolbot schoolbot;
    private Connection dbConnection;
    private final ConfigHandler configHandler;

    public DatabaseHandler(Schoolbot schoolbot)
    {
        this.schoolbot = schoolbot;
        this.configHandler = schoolbot.getConfigHandler();
        initConnection();
    }

    public void initConnection()
    {

        try
        {
            dbConnection = DriverManager.getConnection("jdbc:mysql://" +
                            configHandler.getString(ConfigOption.DBHOSTNAME),
                    configHandler.getString(ConfigOption.DBUSER),
                    configHandler.getString(ConfigOption.DBPASSWORD));
        }
        catch (Exception e)
        {
            schoolbot.getLogger().info("Database could not connect correctly!");
            e.printStackTrace();
            System.exit(1);
        }
    }


    public Connection getDbConnection()
    {
        initConnection();
        return dbConnection;
    }
}




