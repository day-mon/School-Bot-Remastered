package schoolbot.natives.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.school.School;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);

    public DatabaseUtil()
    {
    }


    public static List<School> getSchools(Schoolbot schoolBot)
    {
        List<School> schools = new ArrayList<>();
        School school;
        ResultSet rs;
        try
        {
            Statement statement = schoolBot.getDatabaseHandler().getDbConnection().createStatement();
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
            return schools;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addSchool(Schoolbot schoolBot, String school_id, String school_email_suffix, long role_id, long guild_id)
    {
        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {

            PreparedStatement statement = con.prepareStatement
                    ("INSERT INTO schools " +
                            "(school_name, role_id, school_email_suffix, guild_id) " +
                            "VALUES (?, ?, ?, ?)");
            statement.setString(1, school_id);
            statement.setLong(2, role_id);
            statement.setString(3, school_email_suffix);
            statement.setLong(4, guild_id);
            statement.execute();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeSchool(Schoolbot schoolBot, String schoolName)
    {
        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {

            PreparedStatement statement = con.prepareStatement(
                    "DELETE FROM schools WHERE school_name=?"
            );
            statement.setString(1, schoolName);
            statement.execute();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

    }


}
