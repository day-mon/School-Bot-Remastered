package schoolbot.natives.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DatabaseUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);

    //TODO: Fix spelling of capacity
    public DatabaseUtil()
    {
    }


    public static boolean getClassesBySchoolID(Schoolbot schoolbot, int id)
    {
        ResultSet resultSet = null;

        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement preparedStatement = con.prepareStatement("SELECT class.school_id FROM class WHERE class.school_id=?");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return false;
        }
    }


    public static List<Classroom> getSchoolByID(Schoolbot schoolbot, String className, long guild_id)
    {
        List<Classroom> classroomList = new ArrayList<>();
        ResultSet resultSet = null;

        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM class WHERE name=? AND guild_id=?");
            resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                classroomList.add(new Classroom(
                        resultSet.getInt("id"),
                        resultSet.getString("name")
                ));
            }
            return classroomList;
        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return Collections.emptyList();
        }
    }


    public static School getSpecificSchoolBySchoolName(Schoolbot schoolbot, String schoolName, long guild_id)
    {
        School school = null;
        ResultSet rs;

        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM schools WHERE guild_id=? AND name=?");
            statement.setLong(1, guild_id);
            statement.setString(2, schoolName);
            rs = statement.executeQuery();

            while (rs.next())
            {
                school = new School();
                school.setSchoolName(rs.getString("name"));
                school.setRoleID(rs.getLong("role_id"));
                school.setEmailSuffix(rs.getString("email_suffix"));
                school.setGuildID(rs.getLong("guild_id"));
                school.setIsPittSchool(rs.getBoolean("is_pitt_campus"));
            }
            return school;
        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return null;
        }
    }

    public static School getSpecificSchoolByID(Schoolbot schoolbot, int id, long guild_id)
    {
        School school = null;
        ResultSet rs;

        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM schools WHERE guild_id=? AND id=?");
            statement.setLong(1, guild_id);
            statement.setInt(2, id);
            rs = statement.executeQuery();

            while (rs.next())
            {
                school = new School();
                school.setSchoolName(rs.getString("name"));
                school.setRoleID(rs.getLong("role_id"));
                school.setEmailSuffix(rs.getString("email_suffix"));
                school.setGuildID(rs.getLong("guild_id"));
                school.setIsPittSchool(rs.getBoolean("is_pitt_campus"));
            }
            return school;
        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return school;
        }
    }

    public static List<Professor> getProfessorsWithClassInformation(Schoolbot schoolbot, long guildId, int schoolID)
    {
        HashMap<String, Professor> professorHashMap = new HashMap<>();
        ResultSet resultSet = null;
        Professor professor = null;
        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("""
                       SELECT professors.first_name, professors.last_name, class.name, professors.email_prefix
                       from CLASS  
                       INNER JOIN professors on professors.id=class.instructor_id
                       WHERE class.guild_id=?
                       AND class.school_id=?                                                  
                    """);
            statement.setLong(1, guildId);
            statement.setInt(2, schoolID);
            resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                String emailPrefix = resultSet.getString("email_prefix");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String firstAndLast = firstName + " " + lastName;

                if (professorHashMap.containsKey(firstAndLast))
                {
                    professorHashMap.get(firstAndLast).increaseClassCount();
                }
                else
                {
                    professorHashMap.put(firstAndLast, new Professor(firstName, lastName, emailPrefix));
                }
            }

            return new ArrayList<>(professorHashMap.values());


        }
        catch (Exception exception)
        {
            LOGGER.error("Database error", exception);
            return Collections.emptyList();
        }
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
                school.setSchoolName(rs.getString("name"));
                school.setRoleID(rs.getLong("role_id"));
                school.setEmailSuffix(rs.getString("email_suffix"));
                school.setGuildID(rs.getLong("guild_id"));
                school.setIsPittSchool(rs.getBoolean("is_pitt_campus"));
                school.setSchoolID(rs.getInt("id"));
                schools.add(school);
            }
            return schools;
        }
        catch (SQLException e)
        {
            LOGGER.error("Database error", e);
            return Collections.emptyList();
        }

    }

    public static int getProfessorsID(Schoolbot schoolbot, String name)
    {
        int i = 0;
        ResultSet rs = null;
        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM professors WHERE first_and_last=?");
            statement.setString(1, name);
            rs = statement.executeQuery();

            while (rs.next())
            {
                i = rs.getInt("id");
            }
            return i;

        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return -1;
        }
    }


    public static int getSchoolID(Schoolbot schoolbot, String name)
    {
        int i = 0;
        ResultSet rs = null;
        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM schools WHERE name=?");
            statement.setString(1, name);
            rs = statement.executeQuery();

            while (rs.next())
            {
                i = rs.getInt("id");
                return i;
            }
            return -1;

        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return -1;
        }
    }


    public static List<Professor> getProfessors(Schoolbot schoolBot, int school_id, long guild_id)
    {
        List<Professor> professors = new ArrayList<>();
        Professor professor;
        ResultSet rs;

        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM professors WHERE school_id=?");
            statement.setInt(1, school_id);
            rs = statement.executeQuery();


            while (rs.next())
            {
                professor = new Professor();
                professor.setFirstName(rs.getString("first_name"));
                professor.setLastName(rs.getString("last_name"));
                professor.setEmailPrefix(rs.getString("email_prefix"));
                professor.setId(rs.getInt("id"));
                professor.setSchoolID(rs.getInt("school_id"));

                professors.add(professor);
            }
            return professors;
        }

        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return Collections.emptyList();

        }
    }

    public static boolean addClassPitt(Schoolbot schoolbot, Classroom clazz, long guild_id)
    {
        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            // 1    2      3      4               5         6           7            8         9        10        11         12          13          14    15       16            17         19
            PreparedStatement statement = con.prepareStatement("INSERT INTO class (name, number, room, instructor_id, location, seats_taken, seats_open, capacity, status, start_date, end_date, meet_time, description, preqs, level, school_id, identifier, guild_id) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            schoolbot.getLogger().info("class : {}", clazz.toString());


            statement.setString(1, clazz.getClassName());
            statement.setInt(2, clazz.getClassNumber());
            statement.setString(3, clazz.getClassRoom());
            statement.setInt(4, clazz.getProfessorID());
            statement.setString(5, clazz.getClassLocation());
            statement.setInt(6, clazz.getSeatsTaken());
            statement.setInt(7, clazz.getSeatsOpen());
            statement.setInt(8, clazz.getClassCapacity());
            statement.setString(9, clazz.getClassStatus());
            statement.setDate(10, new java.sql.Date(LocalDate.parse(clazz.getInputClassStartDate(), DateTimeFormatter.ofPattern("M/dd/yyyy")).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()));
            statement.setDate(11, new java.sql.Date(LocalDate.parse(clazz.getInputClassEndDate(), DateTimeFormatter.ofPattern("M/dd/yyyy")).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()));
            statement.setString(12, clazz.getClassTime());
            statement.setString(13, clazz.getDescription());
            statement.setString(14, clazz.getPreReq());
            statement.setString(15, clazz.getClassLevel());
            statement.setInt(16, clazz.getSchoolID());
            statement.setString(17, clazz.getClassIdentifier());
            statement.setLong(18, guild_id);
            statement.execute();
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return false;
        }
    }

    public static List<Classroom> getClasses(Schoolbot schoolBot, long guildID)
    {
        List<Classroom> classes = new ArrayList<>();
        Classroom clazz;
        ResultSet rs;

        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM class WHERE guild_id=?");
            statement.setLong(1, guildID);
            rs = statement.executeQuery();


            while (rs.next())
            {
                clazz = new Classroom();

                clazz.setClassName(rs.getString("name"));
                classes.add(clazz);
            }
            return classes;

        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
        }
        return classes;
    }


    public static List<Classroom> getClassByClassName(Schoolbot schoolBot, String schoolName, long guildID)
    {
        List<Classroom> classes = new ArrayList<>();
        Classroom clazz;
        ResultSet rs;

        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM class WHERE guild_id=? AND name=?");
            statement.setLong(1, guildID);
            statement.setString(2, schoolName);
            rs = statement.executeQuery();


            while (rs.next())
            {
                clazz = new Classroom();
                clazz.setClassName(rs.getString("name"));
                classes.add(clazz);
            }
            return classes;

        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return classes;
        }

    }


    public static boolean addSchool(Schoolbot schoolBot, String school_id, String school_email_suffix, long role_id, long guild_id)
    {
        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {

            PreparedStatement statement = con.prepareStatement
                    ("INSERT INTO schools " +
                            "(name, role_id, email_suffix, guild_id, is_pitt_campus) " +
                            "VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, school_id);
            statement.setLong(2, role_id);
            statement.setString(3, school_email_suffix);
            statement.setLong(4, guild_id);
            statement.setBoolean(5, school_id.contains("University of Pittsburgh"));
            statement.execute();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addProfessor(Schoolbot schoolbot, String firstName, String lastName, String emailPrefix, int schoolID, long guildID)
    {
        try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO professors " +
                            "(first_name, last_name, email_prefix, school_id, first_and_last) " +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, emailPrefix);
            statement.setInt(4, schoolID);
            statement.setString(5, firstName + " " + lastName);
            statement.execute();
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return false;
        }
    }


    public static boolean removeSchool(Schoolbot schoolBot, String schoolName)
    {
        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {

            PreparedStatement statement = con.prepareStatement(
                    "DELETE FROM schools WHERE name=?"
            );
            statement.setString(1, schoolName);
            statement.execute();
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return false;
        }
    }

    public static boolean removeProfessor(Schoolbot schoolBot, int id)
    {
        try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
        {
            PreparedStatement statement = con.prepareStatement(
                    "DELETE FROM professors WHERE id?"
            );
            statement.setInt(1, id);
            statement.execute();
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Database error", e);
            return false;
        }
    }
}
