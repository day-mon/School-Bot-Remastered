package schoolbot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.Reminder;
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

public class DatabaseUtils
{


      private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtils.class);

      private DatabaseUtils()
      {
      }

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
                  preparedStatement.setString(3, assignment.getType().getAssignmentType());
                  // this is horrible i will figure out why i did this l8er
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
                  LOGGER.error("Database error has occurred while removing an assignment", e);
            }

      }

      public static void removeAssignmentReminderByAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  removeReminderByAssignmentID(connection, assignment.getId());
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error has occurred while  removing an assignment reminder", e);
            }
      }

      public static void removeClassReminderByClass(Schoolbot schoolbot, Classroom classroom)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  removeClassReminders(connection, classroom.getId());
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error has occurred while removing an assignment reminder", e);
            }
      }


      public static WrapperReturnValue loadGuild(Schoolbot schoolBot, long guild_id)
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

                  var prefix = grabGuildPrefix(connection, guild_id);


                  return new WrapperReturnValue(schools, classrooms, guild_id, prefix);
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return new WrapperReturnValue();
            }
      }

      private static String grabGuildPrefix(Connection connection, long guild_id) throws SQLException
      {
            PreparedStatement statement = connection.prepareStatement("SELECT prefix FROM guild_settings WHERE guild_id=?");
            statement.setLong(1, guild_id);
            var resultSet = statement.executeQuery();
            return resultSet.next() ? resultSet.getString("prefix") : null;
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
                  statement.setInt(4, professor.getSchoolId());
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


      public static int addNormalClass(CommandEvent event, Classroom clazz)
      {
            Schoolbot schoolbot = event.getSchoolbot();
            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement("""
                          INSERT INTO public.classes(
                           professor_id, start_date, end_date, time,
                           school_id, identifier, term, description, guild_id, name,
                           role_id, channel_id, autofilled)
                           
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                                   
                          returning id
                          """);

                  statement.setInt(1, clazz.getProfessor().getId());
                  statement.setDate(2, Date.valueOf(clazz.getStartDate()));
                  statement.setDate(3, Date.valueOf(clazz.getEndDate()));
                  statement.setString(4, clazz.getTime());
                  statement.setInt(5, clazz.getSchool().getID());
                  statement.setString(6, clazz.getClassIdentifier());
                  statement.setString(7, clazz.getTerm());
                  statement.setString(8, clazz.getDescription());
                  statement.setLong(9, event.getGuild().getIdLong());
                  statement.setString(10, clazz.getName());
                  statement.setLong(11, clazz.getRoleID());
                  statement.setLong(12, clazz.getChannelID());
                  statement.setBoolean(13, clazz.isAutoFilled());
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

      public static int addClassPitt(CommandEvent event, Classroom clazz)
      {
            Schoolbot schoolbot = event.getSchoolbot();
            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = con.prepareStatement("""
                          INSERT INTO public.classes(
                           number, professor_id, location, start_date, end_date, time, preqs,
                           level, school_id, identifier, term, description, guild_id, name,
                           role_id, channel_id, autofilled, room)
                           
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                                   
                          returning id
                          """);

                  statement.setInt(1, clazz.getNumber());
                  statement.setInt(2, clazz.getProfessor().getId());
                  statement.setString(3, clazz.getLocation());
                  statement.setDate(4, Date.valueOf(clazz.getStartDate()));
                  statement.setDate(5, Date.valueOf(clazz.getEndDate()));
                  statement.setString(6, clazz.getTime());
                  statement.setString(7, clazz.getPrerequisite());
                  statement.setString(8, clazz.getLevel());
                  statement.setInt(9, clazz.getSchool().getID());
                  statement.setString(10, clazz.getClassIdentifier());
                  statement.setString(11, clazz.getTerm());
                  statement.setString(12, clazz.getDescription());
                  statement.setLong(13, event.getGuild().getIdLong());
                  statement.setString(14, clazz.getName());
                  statement.setLong(15, clazz.getRoleID());
                  statement.setLong(16, clazz.getChannelID());
                  statement.setBoolean(17, clazz.isAutoFilled());
                  statement.setString(18, clazz.getRoom());
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
                  statement.setBoolean(5, school.isPittSchool());
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
            int id = professor.getId();
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

      public static void removeClassroom(Schoolbot schoolbot, Classroom classroom)
      {
            int id = classroom.getId();
            try (Connection con = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  removeClassReminders(con, id);
                  PreparedStatement statement = con.prepareStatement("DELETE FROM classes WHERE id=?");
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

      private static void removeReminderByAssignmentID(Connection connection, int assignmentID) throws SQLException
      {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM assignments_reminders WHERE assignment_id=?");
            preparedStatement.setInt(1, assignmentID);
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


      public static List<Reminder> classReminder(Schoolbot schoolbot)
      {
            List<Reminder> reminders = new ArrayList<>();
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM class_reminders WHERE remind_time < now()");
                  ResultSet resultSet = preparedStatement.executeQuery();

                  while (resultSet.next())
                  {

                        var reminderId = resultSet.getInt("id");
                        var classId = resultSet.getInt("class_id");
                        var clazz = getClassFromID(connection, classId);

                        reminders.add(new Reminder(reminderId, clazz));
                  }

                  return reminders;

            }
            catch (Exception e)
            {
                  LOGGER.error("Database error has occurred", e);
                  return Collections.emptyList();
            }
      }

      public static void removeReminder(Schoolbot schoolbot, Reminder reminder)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  String reminderType = reminder.obj().getClass().getSimpleName();
                  String tableName = reminderType.equalsIgnoreCase("Assignment") ?
                          "assignments_reminders" : "class_reminders";
                  PreparedStatement statement = connection.prepareStatement(
                          String.format("delete from %s where id=?", tableName)
                  );

                  statement.setInt(1, reminder.id());
                  statement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database Error", e);
            }
      }

      public static boolean lastClassReminder(Schoolbot schoolbot, Reminder reminder)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement statement = connection.prepareStatement("SELECT FROM class_reminders WHERE id=?");
                  statement.setInt(1, reminder.id());
                  ResultSet resultSet = statement.executeQuery();


                  return resultSet.next();
            }
            catch (Exception e)
            {
                  LOGGER.error("Database Error", e);
                  return false;
            }

      }

      public static void updateSchool(DatabaseDTO schoolUpdateDTO, Schoolbot schoolbot)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("UPDATE schools SET " + schoolUpdateDTO.updateColumn() + "= ? WHERE id=?");
                  School school = (School) schoolUpdateDTO.objectBeingUpdated();
                  preparedStatement.setObject(1, schoolUpdateDTO.valueBeingChanged());
                  preparedStatement.setInt(2, school.getID());
                  preparedStatement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Error occurred while updating schools", e);
            }
      }

      public static void updateProfessor(DatabaseDTO schoolUpdateDTO, Schoolbot schoolbot)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("UPDATE professors SET " + schoolUpdateDTO.updateColumn() + "= ? WHERE id=?");
                  Professor professor = (Professor) schoolUpdateDTO.objectBeingUpdated();
                  preparedStatement.setObject(1, schoolUpdateDTO.valueBeingChanged());
                  preparedStatement.setInt(2, professor.getId());
                  preparedStatement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Error occurred while updating professors", e);
            }
      }

      public static void updateClassroom(DatabaseDTO classroomUpdateDTO, Schoolbot schoolbot)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("UPDATE classes SET " + classroomUpdateDTO.updateColumn() + "= ? WHERE id=?");
                  Classroom classroom = (Classroom) classroomUpdateDTO.objectBeingUpdated();
                  preparedStatement.setObject(1, classroomUpdateDTO.valueBeingChanged());
                  preparedStatement.setInt(2, classroom.getId());
                  preparedStatement.execute();
            }
            catch (Exception e)
            {
                  LOGGER.error("Error occurred while updating professors", e);
            }
      }

      public static boolean updatePrefix(String prefix, CommandEvent event)
      {
            var schoolbot = event.getSchoolbot();
            var guildId = event.getGuild().getIdLong();
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {

                  PreparedStatement statement = connection.prepareStatement("""
                          INSERT INTO guild_settings (guild_id, prefix)
                          VALUES (?, ?) ON CONFLICT (guild_id)
                          DO UPDATE SET prefix=?;
                           """);
                  statement.setLong(1, guildId);
                  statement.setString(2, prefix);
                  statement.setString(3, prefix);
                  statement.execute();
                  return true;
            }
            catch (Exception e)
            {
                  LOGGER.error("Database Error", e);
                  return false;
            }
      }


      public static void updateAssignment(DatabaseDTO assignmentUpdateDTO, Schoolbot schoolbot)
      {
            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  PreparedStatement preparedStatement = connection.prepareStatement("UPDATE assignments SET " + assignmentUpdateDTO.updateColumn() + "= ? WHERE id=?");
                  Assignment assignment = (Assignment) assignmentUpdateDTO.objectBeingUpdated();
                  if (assignmentUpdateDTO.valueBeingChanged().getClass() == Assignment.AssignmentType.class)
                  {
                        String type = ((Assignment.AssignmentType) assignmentUpdateDTO.valueBeingChanged()).getAssignmentType();
                        preparedStatement.setString(1, type);

                  }
                  else
                  {
                        preparedStatement.setObject(1, assignmentUpdateDTO.valueBeingChanged());

                  }
                  preparedStatement.setInt(2, assignment.getId());
                  preparedStatement.execute();

                  if (assignmentUpdateDTO.updateColumn().equals("due_date"))
                  {
                        removeReminderByAssignmentID(connection, assignment.getId());

                        LocalDateTime localDateTime = (LocalDateTime) assignmentUpdateDTO.valueBeingChanged();
                        assignment.setDueDate(localDateTime);

                        addAssignmentReminder(schoolbot, assignment, List.of(1440, 60, 30, 10, 0));
                  }
            }
            catch (Exception e)
            {
                  LOGGER.error("Error occurred while updating assignments", e);
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
                          getClassFromID(connection, resultSet.getInt("class_id")));
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
                          rs.getString("name"),
                          rs.getDate("start_date"),
                          rs.getDate("end_date"),
                          rs.getString("time")
                  );
                  classroom.setId(id);
            }
            return classroom;

      }


      private static List<Classroom> getClasses(Connection conn, School school) throws SQLException
      {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM classes WHERE school_id=?");
            ps.setInt(1, school.getID());
            ResultSet resultSet = ps.executeQuery();
            List<Classroom> out = new ArrayList<>();

            while (resultSet.next())
            {
                  Classroom classroom;
                  int professorID = resultSet.getInt("professor_id");
                  school.addClass(
                          classroom = new Classroom(
                                  resultSet.getString("description"), //You can refactor the resultSet
                                  resultSet.getString("time"),
                                  resultSet.getString("location"),
                                  resultSet.getString("level"),
                                  resultSet.getString("room"),
                                  resultSet.getString("name"),
                                  resultSet.getString("identifier"),
                                  resultSet.getString("term"),
                                  resultSet.getDate("start_date"),
                                  resultSet.getDate("end_date"),
                                  resultSet.getInt("school_id"),
                                  resultSet.getInt("professor_id"),
                                  resultSet.getInt("number"),
                                  resultSet.getInt("id"),
                                  resultSet.getLong("role_id"),
                                  resultSet.getLong("channel_id"),
                                  resultSet.getLong("guild_id"),
                                  school,
                                  school.getProfessorList()
                                          .stream()
                                          .filter(professor -> professor.getId() == professorID)
                                          .findFirst()
                                          .orElseThrow(() -> new IllegalStateException("Professor some how does not exist. Please check your database to make sure you did not manually edit")),
                                  resultSet.getBoolean("autofilled")
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
                          school,
                          rs2.getString("email_prefix"),
                          rs2.getInt("id")
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

      private static void removeGuildSettings(Connection connection, long guildId) throws SQLException
      {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM guild_settings WHERE guild_id=?");
            statement.setLong(1, guildId);
            statement.execute();
      }

      public static boolean removeAllGuildOccurrences(Schoolbot schoolbot, long guildId)
      {

            try (Connection connection = schoolbot.getDatabaseHandler().getDbConnection())
            {
                  var schoolList = schoolbot.getWrapperHandler().getSchools(guildId);

                  for (var school : schoolList)
                  {
                        for (var classroom : school.getClassroomList())
                        {
                              for (var assignment : classroom.getAssignments())
                              {
                                    removeAssignment(schoolbot, assignment);
                              }
                              removeClassroom(schoolbot, classroom);
                        }

                        for (var professor : school.getProfessorList())
                        {
                              removeProfessor(schoolbot, professor);
                        }
                        removeSchool(schoolbot, school.getName());
                  }
                  removeGuildSettings(connection, guildId);
                  schoolbot.getWrapperHandler().removeGuildFromCache(guildId);
                  return true;
            }
            catch (Exception e)
            {
                  LOGGER.error("Database error", e);
                  return false;
            }
      }


      public static record WrapperReturnValue(Map<String, School> schoolMap, List<Classroom> classrooms, long guildID,
                                              String prefix)
      {
            public WrapperReturnValue()
            {
                  this(Collections.emptyMap(), Collections.emptyList(), 0L, null);
            }

      }

}
