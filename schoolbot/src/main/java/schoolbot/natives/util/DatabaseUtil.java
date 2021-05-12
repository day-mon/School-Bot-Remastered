package schoolbot.natives.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.Assignment;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseUtil
{
      private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtil.class);


      public static int addAssignment(Schoolbot schoolbot, Assignment assignment)
      {

            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = con.prepareStatement("""
                          INSERT INTO public.assignments(
                          name, due_date, type, professor_id, points_possible, description, class_id)
                          VALUES (?, ?, ?, ?, ?, ?, ?)
                                                    
                          returning id
                          """);
                  preparedStatement.setString(1, assignment.getName());
                  preparedStatement.setTimestamp(2, Timestamp.valueOf(assignment.getDueDate().toLocalDateTime()));
                  preparedStatement.setString(3, assignment.getAssignmentType().getAssignmentType());
                  preparedStatement.setInt(4, assignment.getProfessorID());
                  preparedStatement.setInt(5, assignment.getPoints());
                  preparedStatement.setString(6, assignment.getDescription());
                  preparedStatement.setInt(7, assignment.getClassroom().getId());
                  preparedStatement.execute();

                  ResultSet assignmentID = preparedStatement.getResultSet();
                  assignmentID.next();
                  return assignmentID.getInt(1);

            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return -1;
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
                        preparedStatement1.setInt(1, school.getID());
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
                        PreparedStatement preparedStatement2 = connection.prepareStatement("SELECT * FROM classes WHERE school_id=?");
                        preparedStatement2.setInt(1, school.getID());
                        rs3 = preparedStatement2.executeQuery();

                        while (rs3.next())
                        {
                              Classroom classroom;
                              int professorID = rs3.getInt("professor_id");
                              school.addClass(
                                      classroom = new Classroom(
                                              rs3.getString("description"),
                                              rs3.getString("time"),
                                              rs3.getString("location"),
                                              rs3.getString("level"),
                                              rs3.getString("room"),
                                              rs3.getString("name"),
                                              rs3.getString("identifier"),
                                              rs3.getString("term"),
                                              rs3.getDate("start_date"),
                                              rs3.getDate("end_date"),
                                              rs3.getInt("school_id"),
                                              rs3.getInt("professor_id"),
                                              rs3.getInt("number"),
                                              rs3.getInt("id"),
                                              rs3.getLong("role_id"),
                                              rs3.getLong("channel_id"),
                                              rs3.getLong("guild_id"),
                                              school,
                                              school.getProfessorList()
                                                      .stream()
                                                      .filter(professor -> professor.getID() == professorID)
                                                      .limit(1)
                                                      .collect(Collectors.toList())
                                                      .get(0)
                                      ));

                              ResultSet rs4 = null;
                              PreparedStatement preparedStatement4 = connection.prepareStatement("SELECT * FROM assignments WHERE class_id=?");
                              preparedStatement4.setInt(1, classroom.getId());
                              rs4 = preparedStatement4.executeQuery();

                              while (rs4.next())
                              {
                                    classroom.addAssignment(
                                            new Assignment(
                                                    rs4.getString("name"),
                                                    rs4.getString("description"),
                                                    rs4.getInt("points_possible"),
                                                    rs4.getInt("professor_id"),
                                                    rs4.getInt("id"),
                                                    rs4.getString("type"),
                                                    rs4.getTimestamp("due_date"),
                                                    classroom
                                            ));
                              }
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


      public static int addProfessor(Schoolbot schoolbot, Professor professor)
      {
            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement(
                          """
                                  INSERT INTO professors
                                  (first_name, last_name, email_prefix, school_id, full_name)
                                  VALUES (?, ?, ?, ?, ?)
                                  returning id
                                  """
                  );
                  statement.setString(1, professor.getFirstName());
                  statement.setString(2, professor.getLastName());
                  statement.setString(3, professor.getEmailPrefix());
                  statement.setInt(4, professor.getSchoolID());
                  statement.setString(5, professor.getFullName());
                  statement.execute();

                  ResultSet professorID = statement.getResultSet();
                  professorID.next();
                  return professorID.getInt(1);
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return -1;
            }
      }


      public static int addClassPitt(CommandEvent event, Classroom clazz)
      {
            int sYear = Integer.parseInt(clazz.getInputClassStartDate()[2]);
            int sMonth = Integer.parseInt(clazz.getInputClassStartDate()[0]);
            int sDay = Integer.parseInt(clazz.getInputClassStartDate()[1]);


            int eYear = Integer.parseInt(clazz.getInputClassEndDate()[2]);
            int eMonth = Integer.parseInt(clazz.getInputClassEndDate()[0]);
            int eDay = Integer.parseInt(clazz.getInputClassEndDate()[1]);

            Schoolbot schoolbot = event.getSchoolbot();
            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement("""
                          INSERT INTO public.classes(
                           number, professor_id, location, start_date, end_date, time, preqs,
                           level, school_id, identifier, term, description, guild_id, name,
                           role_id, channel_id)
                           
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                                   
                          returning id
                          """);

                  statement.setInt(1, clazz.getClassNumber());
                  statement.setInt(2, clazz.getProfessor().getID());
                  statement.setString(3, clazz.getClassLocation());
                  statement.setDate(4, Date.valueOf(LocalDate.of(sYear, sMonth, sDay)));
                  statement.setDate(5, Date.valueOf(LocalDate.of(eYear, eMonth, eDay)));
                  statement.setString(6, clazz.getClassTime());
                  statement.setString(7, clazz.getPreReq());
                  statement.setString(8, clazz.getClassLevel());
                  statement.setInt(9, clazz.getSchool().getID());
                  statement.setString(10, clazz.getClassIdentifier());
                  statement.setString(11, clazz.getTerm());
                  statement.setString(12, clazz.getDescription());
                  statement.setLong(13, event.getGuild().getIdLong());
                  statement.setString(14, clazz.getClassName());
                  statement.setLong(15, clazz.getRoleID());
                  statement.setLong(16, clazz.getChannelID());
                  statement.execute();


                  ResultSet resultSet = statement.getResultSet();
                  resultSet.next();
                  return resultSet.getInt(1);
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return -1;
            }
      }


      public static int addSchool(CommandEvent event, School school)
      {
            Schoolbot schoolbot = event.getSchoolbot();


            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement("""
                          INSERT INTO schools
                          (name, role_id, email_suffix, guild_id, is_pitt_campus, url)
                          VALUES (?, ?, ?, ?, ?, ?)
                          returning id
                          """);
                  statement.setString(1, school.getSchoolName());
                  statement.setLong(2, school.getRoleID());
                  statement.setString(3, school.getEmailSuffix());
                  statement.setLong(4, school.getGuildID());
                  statement.setBoolean(5, school.getIsPittSchool());
                  statement.setString(6, school.getURL());
                  statement.executeQuery();

                  ResultSet schoolsID = statement.getResultSet();
                  schoolsID.next();
                  return schoolsID.getInt(1);
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return -1;
            }
      }


      public static List<Classroom> getAllGuildClasses(Schoolbot schoolbot, long key)
      {
            List<Classroom> classroomList = new ArrayList<>();
            ResultSet resultSet;

            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM class WHERE guild_id = ?");
                  preparedStatement.setLong(1, key);
                  resultSet = preparedStatement.executeQuery();

                  while (resultSet.next())
                  {
                        classroomList.add(new Classroom(
                                resultSet.getString("description"),
                                resultSet.getString("meet_time"),
                                resultSet.getString("location"),
                                resultSet.getString("level"),
                                resultSet.getString("room"),
                                resultSet.getString("name"),
                                resultSet.getString("identifier"),
                                resultSet.getString("term"),
                                resultSet.getDate("start_date"),
                                resultSet.getDate("end_date"),
                                resultSet.getInt("school_id"),
                                resultSet.getInt("instructor_id"),
                                resultSet.getInt("number"),
                                resultSet.getInt("id"),
                                resultSet.getLong("role_id"),
                                resultSet.getLong("channel_id"),
                                resultSet.getLong("guild_id"),
                                null,
                                null));
                  }
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return classroomList;
            }

            return classroomList;
      }

      public static void updateSchool(CommandEvent event, School.SchoolUpdates schoolUpdates, String updateItem, School school)
      {
            Schoolbot schoolbot = event.getSchoolbot();


            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement(schoolUpdates.getUpdateQuery());
                  statement.setString(1, updateItem);
                  statement.setInt(2, school.getID());


            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);

            }
      }


      public static void removeSchool(Schoolbot schoolBot, String schoolName)
      {
            try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
            {

                  PreparedStatement statement = con.prepareStatement(
                          "DELETE FROM schools WHERE name=?"
                  );
                  statement.setString(1, schoolName);
                  statement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
            }
      }

      public static void removeProfessor(Schoolbot schoolBot, Professor professor)
      {
            int id = professor.getID();
            try (Connection con = schoolBot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement(
                          "DELETE FROM professors WHERE id=?"
                  );
                  statement.setInt(1, id);
                  statement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
            }
      }
}
