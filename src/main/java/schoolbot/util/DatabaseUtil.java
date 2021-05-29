package schoolbot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
                  preparedStatement.setTimestamp(2, Timestamp.valueOf(assignment.getDueDate()));
                  preparedStatement.setString(3, assignment.getAssignmentType().getAssignmentType());
                  // this is horible i will figure out why i did this l8er
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

      public static void addClassReminder(Schoolbot schoolbot, LocalDateTime time, List<Integer> intervals, Classroom classroom)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = connection.prepareStatement(
                          """
                                  INSERT INTO class_reminders (class_id, remind_time)
                                  VALUES (?, ?)
                                  """
                  );

                  for (int interval : intervals)
                  {
                        LocalDateTime ldt = time.minusMinutes(interval);

                        statement.setInt(1, classroom.getId());
                        statement.setTimestamp(2, Timestamp.valueOf(ldt));
                        statement.addBatch();
                  }

                  statement.executeBatch();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
            }
      }

      public static void addAssignmentReminder(Schoolbot schoolBot, Assignment assignment, List<Integer> intervals)
      {
            try (Connection connection = schoolBot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement(
                          """
                                      INSERT INTO assignments_reminders
                                      (assignment_id, remind_time) VALUES (?, ?)
                                  """);


                  for (int interval : intervals)
                  {
                        LocalDateTime ldt = assignment.getDueDate().minusMinutes(interval);
                        if (ldt.isAfter(LocalDateTime.now()))
                        {

                              preparedStatement.setInt(1, assignment.getId());
                              preparedStatement.setTimestamp(2, Timestamp.valueOf(ldt));
                              preparedStatement.addBatch();
                        }
                  }
                  preparedStatement.executeBatch();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
            }
      }

      public static void removeAssignment(Schoolbot schoolBot, Assignment assignment)
      {
            try (Connection connection = schoolBot.getDatabaseHandler().getDbConnection())
            {
                  removeAllReminders(connection, assignment);
                  removeAssignment(connection, assignment);
            }
            catch (Exception e)
            {
                  e.printStackTrace();
            }
      }


      public static WrapperReturnValue getSchools(Schoolbot schoolBot, long guild_id)
      {
            Map<String, School> schools = new ConcurrentHashMap<>();
            List<Classroom> classrooms = new ArrayList<>();

            try (Connection connection = schoolBot.getDatabaseHandler().getDbConnection())
            {
                  List<School> schoolList = getSchools(connection, guild_id);

                  for (School sh : schoolList)
                  {
                        getProfessors(connection, sh);
                        List<Classroom> classroomList = getClasses(connection, sh);

                        for (Classroom classroom : classroomList)
                        {
                              getAssignments(connection, classroom);
                        }
                        classrooms.addAll(classroomList);
                        schools.put(sh.getName().toLowerCase(), sh);
                  }

                  return new WrapperReturnValue(schools, classrooms, guild_id);
            }
            catch (SQLException e)
            {
                  LOGGER.error("Database error", e);
                  return new WrapperReturnValue();
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
                  statement.setDate(4, Date.valueOf(clazz.getClassStartDate()));
                  statement.setDate(5, Date.valueOf(clazz.getClassEndDate()));
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
                  statement.setString(1, school.getName());
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

      public static void removeClassroom(CommandEvent event, Classroom classroom)
      {
            int id = classroom.getId();
            Schoolbot schoolbot = event.getSchoolbot();
            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  removeClassReminders(con, id);
                  PreparedStatement statement = con.prepareStatement(
                          "DELETE FROM classes WHERE id=?"
                  );
                  statement.setInt(1, id);
                  statement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
            }

      }

      private static List<School> getSchools(Connection connection, long guildID) throws SQLException
      {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM schools WHERE guild_id=?");
            ps.setLong(1, guildID);
            ResultSet rs = ps.executeQuery();

            List<School> out = new ArrayList<>();

            while (rs.next())
            {
                  School school = new School();
                  school.setName(rs.getString("name"));
                  school.setRoleID(rs.getLong("role_id"));
                  school.setEmailSuffix(rs.getString("email_suffix"));
                  school.setGuildID(rs.getLong("guild_id"));
                  school.setIsPittSchool(rs.getBoolean("is_pitt_campus"));
                  school.setSchoolID(rs.getInt("id"));
                  school.setURL(rs.getString("url"));
                  out.add(school);
            }
            return out;
      }

      private static void removeAssignment(Connection connection, Assignment assignment) throws SQLException
      {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM assignments WHERE id=?");
            preparedStatement.setInt(1, assignment.getId());
            preparedStatement.execute();
      }

      // Could have an interface
      private static void removeAllReminders(Connection connection, Assignment assignment) throws SQLException
      {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM assignments_reminders WHERE assignment_id=?");
            preparedStatement.setInt(1, assignment.getId());
            preparedStatement.execute();
      }

      private static void removeReminder(Connection connection, int id) throws SQLException
      {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM assignments_reminders WHERE id=?");
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
      }

      private static void removeClassReminders(Connection connection, int id) throws SQLException
      {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM class_reminders WHERE class_id=?");
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
      }

      public static List<Assignment> checkRemindTimes(Schoolbot schoolbot)
      {
            List<Assignment> assignments = new ArrayList<>();
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM assignments_reminders WHERE remind_time < now()");
                  ResultSet resultSet = preparedStatement.executeQuery();

                  while (resultSet.next())
                  {
                        int assignment_id = resultSet.getInt("assignment_id");
                        int id = resultSet.getInt("id");

                        Assignment assignment = getAssignmentFromID(connection, assignment_id);
                        assignments.add(assignment);
                        removeReminder(connection, id);

                        if (assignment.getDueDate().isBefore(LocalDateTime.now()))
                        {
                              removeAssignment(connection, assignment);
                        }
                  }

                  return assignments;


            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return Collections.emptyList();
            }
      }

      public static List<Classroom> checkClassRemindTimes(Schoolbot schoolbot)
      {
            List<Classroom> classroomList = new ArrayList<>();
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM class_reminders WHERE remind_time < now()");
                  ResultSet resultSet = preparedStatement.executeQuery();

                  while (resultSet.next())
                  {
                        int classID = resultSet.getInt("class_id");
                        int id = resultSet.getInt("id");
                        classroomList.add(getClassFromID(connection, classID));
                        removeReminder(connection, id);
                  }

                  return classroomList;


            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return Collections.emptyList();
            }
      }

      public static void updateSchool(DatabaseDTO schoolUpdateDTO, Schoolbot schoolbot)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("UPDATE schools SET " + schoolUpdateDTO.updateColumn() + "= ? WHERE id=?");
                  School school = (School) schoolUpdateDTO.value();
                  preparedStatement.setObject(1, schoolUpdateDTO.value());
                  preparedStatement.setInt(2, school.getID());
                  preparedStatement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Error occurred while updating schools", e);
            }
      }


      private static Assignment getAssignmentFromID(Connection connection, int assignmentID) throws SQLException
      {
            Assignment assignment = null;
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM assignments WHERE id=?");
            preparedStatement.setInt(1, assignmentID);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next())
            {
                  assignment = new Assignment(
                          resultSet.getString("name"),
                          resultSet.getString("description"),
                          resultSet.getInt("points_possible"),
                          resultSet.getInt("professor_id"),
                          resultSet.getInt("id"),
                          resultSet.getString("type"),
                          resultSet.getTimestamp("due_date"),
                          getClassFromID(connection, resultSet.getInt("class_id"))
                  );
            }

            return assignment;
      }

      private static Classroom getClassFromID(Connection connection, int id) throws SQLException
      {
            Classroom classroom = null;
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM classes WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();


            while (rs.next())
            {
                  classroom = new Classroom(
                          rs.getLong("channel_id"),
                          rs.getLong("role_id"),
                          rs.getString("name")
                  );
            }
            return classroom;

      }


      private static List<Classroom> getClasses(Connection conn, School school) throws SQLException
      {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM classes WHERE school_id=?");
            ps.setInt(1, school.getID());
            ResultSet rs3 = ps.executeQuery();
            List<Classroom> out = new ArrayList<>();

            while (rs3.next())
            {
                  Classroom classroom;
                  int professorID = rs3.getInt("professor_id");
                  school.addClass(
                          classroom = new Classroom(
                                  rs3.getString("description"), //You can refactor the rs3
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
                          )
                  );
                  out.add(classroom);
            }
            return out;
      }

      private static void getProfessors(Connection connection, School school) throws SQLException
      {

            PreparedStatement preparedStatement1 = connection.prepareStatement("SELECT * FROM professors WHERE school_id=?");
            preparedStatement1.setInt(1, school.getID());
            ResultSet rs2 = preparedStatement1.executeQuery();


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
      }

      private static void getAssignments(Connection connection, Classroom classroom) throws SQLException
      {
            PreparedStatement preparedStatement4 = connection.prepareStatement("SELECT * FROM assignments WHERE class_id=?");
            preparedStatement4.setInt(1, classroom.getId());
            ResultSet rs4 = preparedStatement4.executeQuery();

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


      public static record WrapperReturnValue(Map<String, School> schoolMap, List<Classroom> classrooms, long guildID)
      {
            public WrapperReturnValue()
            {
                  this(Collections.emptyMap(), Collections.emptyList(), 0L);
            }

      }

}
