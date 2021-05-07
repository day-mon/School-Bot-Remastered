package schoolbot.natives.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.school.Assignment;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DatabaseUtil
{
      private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);

      /**
       * This method is only to test to see if the connection is valid.. Only for when I am modifying things in a queue callback..
       *
       * @return true if connection is alive..
       */
      public static boolean testConnection(Schoolbot schoolbot)
      {
            try
            {
                  Connection connection = schoolbot.getDatabaseHandler().getDbConnection();
                  connection.close();
                  return true;
            }
            catch (Exception e)
            {
                  LOGGER.error("Cannot connect to database", e);
                  return false;
            }
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

      public static boolean addAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            ResultSet resultSet = null;

            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = con.prepareStatement("""
                          INSERT INTO public.assignments(
                          name, due_date, type, professor_id, points_possible, description, time_due)
                          VALUES (?, ?, ?, ?, ?, ?, ?)
                          """);
                  preparedStatement.setString(1, assignment.getName());
                  preparedStatement.setDate(2, Date.valueOf(assignment.getDueDate()));
                  preparedStatement.setString(3, assignment.getAssignmentType().getAssignmentType());
                  preparedStatement.setInt(4, assignment.getProfessorID());
                  preparedStatement.setInt(5, assignment.getPoints());
                  preparedStatement.setString(6, assignment.getDescription());
                  preparedStatement.setTime(7, Time.valueOf(assignment.getOffsetTime().toLocalTime()));
                  preparedStatement.execute();
                  con.close();
                  return true;

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

      public static boolean checkClassInTerm(Schoolbot schoolbot, int classNumber, String term, long guild_id, int school_id)
      {
            ResultSet resultSet = null;

            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = connection.prepareStatement("SELECT class.name, class.term FROM class WHERE term=? AND number=? AND guild_id=? AND school_id=?");
                  statement.setString(1, term);
                  statement.setInt(2, classNumber);
                  statement.setLong(3, guild_id);
                  statement.setInt(4, school_id);
                  resultSet = statement.executeQuery();

                  return resultSet.next();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database Error", e);
                  return false;
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


      public static Map<String, School> getSchools(Schoolbot schoolBot, long guild_id)
      {
            Map<String, School> schools = new HashMap<>();
            School school;
            ResultSet rs;
            try (Connection connection = schoolBot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM schools WHERE guild_id=?");
                  preparedStatement.setLong(1, guild_id);
                  rs = preparedStatement.executeQuery();

                  while (rs.next())
                  {
                        school = new School();
                        school.setSchoolName(rs.getString("name"));
                        school.setRoleID(rs.getLong("role_id"));
                        school.setEmailSuffix(rs.getString("email_suffix"));
                        school.setGuildID(rs.getLong("guild_id"));
                        school.setIsPittSchool(rs.getBoolean("is_pitt_campus"));
                        school.setSchoolID(rs.getInt("id"));
                        school.setURL(rs.getString("url"));


                        ResultSet rs2 = null;
                        PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT * FROM professors WHERE school_id=?");
                        preparedStatement1.setInt(1, school.getSchoolID());
                        rs2 = preparedStatement1.executeQuery();


                        while (rs2.next())
                        {
                              school.addProfessor(new Professor(
                                      rs2.getString("first_name"),
                                      rs2.getString("last_name"),
                                      rs2.getString("email_prefix"),
                                      rs2.getInt("id"),
                                      school

                              ));
                        }


                        ResultSet rs3 = null;
                        PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT * FROM class WHERE class.school_id=?");
                        preparedStatement2.setInt(1, school.getSchoolID());
                        rs3 = preparedStatement2.executeQuery();

                        while (rs3.next())
                        {
                              school.addClass(new Classroom(
                                      rs3.getString("description"),
                                      rs3.getString("meet_time"),
                                      rs3.getString("location"),
                                      rs3.getString("level"),
                                      rs3.getString("room"),
                                      rs3.getString("name"),
                                      rs3.getString("identifier"),
                                      rs3.getString("term"),
                                      rs3.getDate("start_date"),
                                      rs3.getDate("end_date"),
                                      rs3.getInt("school_id"),
                                      rs3.getInt("instructor_id"),
                                      rs3.getInt("number"),
                                      rs3.getInt("id"),
                                      rs3.getLong("role_id"),
                                      rs3.getLong("channel_id"),
                                      rs3.getLong("guild_id"),
                                      school));
                        }


                        schools.put(school.getSchoolName().toLowerCase(), school);
                  }
                  return schools;
            }
            catch (SQLException e)
            {
                  LOGGER.error("Database error", e);
                  return Collections.emptyMap();
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

      public static void addProfessor(Schoolbot schoolbot, Professor professor)
      {
            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement(
                          "INSERT INTO professors " +
                                  "(first_name, last_name, email_prefix, school_id, first_and_last) " +
                                  "VALUES (?, ?, ?, ?, ?)"
                  );
                  statement.setString(1, professor.getFirstName());
                  statement.setString(2, professor.getLastName());
                  statement.setString(3, professor.getEmailPrefix());
                  //TODO: This is high key horrible. Find a better way later...
                  statement.setInt(4, DatabaseUtil.getSchoolID(schoolbot, professor.getProfessorsSchool().getSchoolName()));
                  statement.setString(5, professor.getFullName());
                  statement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
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
                  PreparedStatement statement = con.prepareStatement("INSERT INTO class (name, number, room, instructor_id, location, seats_taken, seats_open, capacity, status, start_date, end_date, meet_time, description, preqs, level, school_id, identifier, guild_id, term) " +
                          "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

                  int sYear = Integer.parseInt(clazz.getInputClassStartDate()[2]);
                  int sMonth = Integer.parseInt(clazz.getInputClassStartDate()[0]);
                  int sDay = Integer.parseInt(clazz.getInputClassStartDate()[1]);



                  int eYear = Integer.parseInt(clazz.getInputClassEndDate()[2]);
                  int eMonth = Integer.parseInt(clazz.getInputClassEndDate()[0]);
                  int eDay = Integer.parseInt(clazz.getInputClassEndDate()[1]);

                  statement.setString(1, clazz.getClassName());
                  statement.setInt(2, clazz.getClassNumber());
                  statement.setString(3, clazz.getClassRoom());
                  statement.setInt(4, DatabaseUtil.getProfessorsID(schoolbot, clazz.getProfessor().getFullName()));
                  statement.setString(5, clazz.getClassLocation());
                  statement.setInt(6, clazz.getSeatsTaken());
                  statement.setInt(7, clazz.getSeatsOpen());
                  statement.setInt(8, clazz.getClassCapacity());
                  statement.setString(9, clazz.getClassStatus());
                  statement.setDate(10, Date.valueOf(LocalDate.of(sYear, sMonth, sDay)));
                  statement.setDate(11, Date.valueOf(LocalDate.of(eYear, eMonth, eDay)));
                  statement.setString(12, clazz.getClassTime());
                  statement.setString(13, clazz.getDescription());
                  statement.setString(14, clazz.getPreReq());
                  statement.setString(15, clazz.getClassLevel());
                  statement.setInt(16, clazz.getSchoolID());
                  statement.setString(17, clazz.getClassIdentifier());
                  statement.setLong(18, guild_id);
                  statement.setString(19, clazz.getTerm());
                  statement.execute();
                  return true;
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return false;
            }
      }

      public static List<Classroom> getClassesWithinSchool(Schoolbot schoolBot, long guildID, int schoolID)
      {
            List<Classroom> classes = new ArrayList<>();
            Classroom clazz;
            ResultSet rs;

            try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement("SELECT * FROM class WHERE guild_id=? AND school_id=?");
                  statement.setLong(1, guildID);
                  statement.setInt(2, schoolID);
                  rs = statement.executeQuery();


                  while (rs.next())
                  {
                        clazz = new Classroom();

                        clazz.setClassName(rs.getString("name"));
                        clazz.setSchoolID(rs.getInt("school_id"));
                        clazz.setClassEndDate(rs.getDate("end_date"));
                        clazz.setClassStartDate(rs.getDate("start_date"));
                        clazz.setDescription(rs.getString("description"));
                        clazz.setClassNumber(rs.getInt("number"));
                        clazz.setId(rs.getInt("id"));
                        clazz.setProfessorID(rs.getInt("instructor_id"));
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
                  return classes;
            }
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

      public static boolean addSchool(Schoolbot schoolBot, School school)
      {
            try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement("""
                          INSERT INTO schools
                          (name, role_id, email_suffix, guild_id, is_pitt_campus, url)
                           VALUES (?, ?, ?, ?, ?, ?)
                          """);
                  statement.setString(1, school.getSchoolName());
                  statement.setLong(2, school.getRoleID());
                  statement.setString(3, school.getEmailSuffix());
                  statement.setLong(4, school.getGuildID());
                  statement.setBoolean(5, school.getIsPittSchool());
                  statement.setString(6, school.getURL());
                  statement.execute();
                  return true;
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return false;
            }
      }


      public static boolean addSchool(Schoolbot schoolBot, String school_id, String school_email_suffix, long role_id, long guild_id, String URL)
      {
            try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
            {

                  PreparedStatement statement = con.prepareStatement
                          ("INSERT INTO schools " +
                                  "(name, role_id, email_suffix, guild_id, is_pitt_campus, url) " +
                                  "VALUES (?, ?, ?, ?, ?, ?)");
                  statement.setString(1, school_id);
                  statement.setLong(2, role_id);
                  statement.setString(3, school_email_suffix);
                  statement.setLong(4, guild_id);
                  statement.setBoolean(5, school_id.contains("University of Pittsburgh"));
                  statement.setString(6, URL);
                  statement.execute();
                  return true;
            }
            catch (SQLException e)
            {
                  LOGGER.error("Database error", e);
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

      public static boolean removeProfessor(Schoolbot schoolBot, Professor professor)
      {
            int id = DatabaseUtil.getProfessorsID(schoolBot, professor.getFullName());
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
