package schoolbot.handlers;

import net.dv8tion.jda.api.EmbedBuilder;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.config.ConfigOption;
import schoolbot.natives.objects.school.School;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private Schoolbot schoolbot;
    private Connection dbConnection;
    private ConfigHandler configHandler;

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
        return dbConnection;
    }
}




