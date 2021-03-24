package schoolbot.natives.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;

import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);
    public DatabaseUtil(){};

    public boolean writeToTable(String table, String query)
    {
        try
        {

          // Statement statement = dbConnection.createStatement();
            String SQLString = String.format("INSERT INTO %s VALUES %s", table, query);
            //statement.executeUpdate(SQLString);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.info("Could not write to table " +
                    "\n" + e.getMessage());
            return false;
        }
    }
}
