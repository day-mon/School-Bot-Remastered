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
            dbConnection = DriverManager.getConnection("jdbc:mysql://" + configHandler.getString(ConfigOption.DBHOSTNAME), configHandler.getString(ConfigOption.DBUSER), configHandler.getString(ConfigOption.DBPASSWORD));
        }
        catch (Exception e)
        {
            schoolbot.getLogger().info("Database could not connect correctly!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean writeToTable(String table, String query)
    {
        try
        {
            PreparedStatement myStatement =
                    dbConnection.prepareStatement("INSERT INTO ? VALUES ?");
            // Statement statement = dbConnection.createStatement();
            String SQLString = String.format("INSERT INTO %s VALUES %s", table, query);
            PreparedStatement statement = dbConnection.prepareStatement(SQLString);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFromTable(String table, String query)
    {
        try (Statement statement = dbConnection.createStatement())
        {
            String SQLString = String.format("DELETE FROM %s WHERE %s", table, query);
            statement.execute(SQLString);
            return true;
        }
        catch (Exception e)
        {
            System.out.println("failed");
            e.printStackTrace();
            return false;
        }
    }


    public List<School> getSchools()
    {
        List<School> schools = new ArrayList<>();
        School school = null;
        ResultSet rs = null;
        try
        {
            Statement statement = dbConnection.createStatement();
            rs = statement.executeQuery("SELECT * FROM schools");

            while (rs.next())
            {
                school = new School();
                school.setSchoolName(rs.getString("school_name"));
                school.setRoleID(rs.getLong("role_id"));
                school.setEmailSuffix(rs.getString("school_email_suffix"));
                school.setGuildID(rs.getLong("guild_id"));
                schools.add(school);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }


        return schools;
    }


    public Connection getDbConnection()
    {
        return dbConnection;
    }
}




