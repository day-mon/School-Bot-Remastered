package schoolbot.objects.guild;

import net.dv8tion.jda.api.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Constants;
import schoolbot.Schoolbot;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.DatabaseUtils;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuildWrapper
{
      private final long guildId;
      private final Map<String, School> schoolList;
      private final List<Classroom> classrooms;
      private String guildPrefix;
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


      public GuildWrapper(DatabaseUtils.WrapperReturnValue data)
      {
            guildId = data.guildID();
            schoolList = data.schoolMap();
            classrooms = Collections.synchronizedList(data.classrooms());
            guildPrefix = data.prefix() == null ? Constants.DEFAULT_PREFIX : data.prefix();
      }

      /*
            This is meant to serve kinda as a cache..
            Each Guild will have there own wrapper so I can easily list, add, edit, and remove schools without having to make many database connections
            Things like ListSchools, ListProfessor, ListAssignments, and ListClasses should not have database calls.. Not really needed and slow
       */

      public void addSchool(CommandEvent event, School school)
      {
            String lowerCaseSchoolName = school.getName().toLowerCase();
            if (schoolList.containsKey(lowerCaseSchoolName)) return;

            int id = DatabaseUtils.addSchool(event, school);
            if (id == -1) return;

            school.setSchoolID(id);
            schoolList.put(lowerCaseSchoolName, school);

      }

      public School getSchool(String schoolName)
      {
            return schoolList.get(schoolName.toLowerCase());
      }

      public void updateSchool(CommandEvent event, DatabaseDTO schoolUpdateDTO)
      {
            String update = schoolUpdateDTO.updateColumn();
            School school = (School) schoolUpdateDTO.objectBeingUpdated();
            String schoolName = school.getName().toLowerCase();

            switch (update)
            {
                  case "name" -> {
                        schoolList.remove(schoolName);

                        String updatedName = (String) schoolUpdateDTO.valueBeingChanged();
                        school.setName(updatedName);
                        schoolList.put(update.toLowerCase(), school);
                  }
                  case "role_id" -> {
                        Role role = event.getJDA().getRoleById(school.getRoleID());

                        if (role != null)
                        {
                              role.delete().queue(
                                      success ->
                                              LOGGER.info("Role for {} successfully has been deleted", school.getName()),
                                      failure ->
                                              LOGGER.error("Role for {} could not be deleted", school.getName(), failure));
                        }
                        Long newRoleID = (Long) schoolUpdateDTO.valueBeingChanged();
                        schoolList.get(schoolName).setRoleID(newRoleID);

                  }

                  case "url" -> {
                        // Could check if valid URL.... but..
                        String URL = (String) schoolUpdateDTO.valueBeingChanged();
                        schoolList.get(schoolName).setURL(URL);
                  }

                  case "email_suffix" -> {
                        String email = (String) schoolUpdateDTO.valueBeingChanged();
                        schoolList.get(schoolName).setEmailSuffix(email);
                  }
            }
            DatabaseUtils.updateSchool(schoolUpdateDTO, event.getSchoolbot());
      }

      public void updateClassroom(CommandEvent event, DatabaseDTO classroomDTO)
      {
            var classroom = (Classroom) classroomDTO.objectBeingUpdated();
            var school = classroom.getSchool();
            var schoolbot = event.getSchoolbot();

            String update = classroomDTO.updateColumn();
            String schoolName = school.getName().toLowerCase();

            int classId = classroom.getId();

            switch (update)
            {
                  case "name" -> {
                        String updateElement = (String) classroomDTO.valueBeingChanged();

                        schoolList.get(schoolName)
                                .getClassroomByID(classId)
                                .setName(updateElement);
                  }

                  case "description" -> {
                        String updateElement = (String) classroomDTO.valueBeingChanged();

                        schoolList.get(schoolName)
                                .getClassroomByID(classId)
                                .setDescription(updateElement);
                  }

                  case "professor_id" -> {
                        int professorId = (int) classroomDTO.valueBeingChanged();
                        Professor professor = schoolList.get(schoolName)
                                .getProfessorByID(professorId);

                        schoolList.get(schoolName)
                                .getClassroomByID(classId)
                                .setProfessor(professor);
                  }


                  case "start_date", "end_date" -> {
                        LocalDateTime ld = (LocalDateTime) classroomDTO.valueBeingChanged();
                        if (update.contains("start"))
                        {
                              schoolList.get(schoolName)
                                      .getClassroomByID(classId)
                                      .setStartDate(Date.valueOf(ld.toLocalDate()));
                        }
                        else
                        {
                              schoolList.get(schoolName)
                                      .getClassroomByID(classId)
                                      .setEndDate(Date.valueOf(ld.toLocalDate()));
                        }
                  }

                  case "number" -> {
                        int classNumber = (int) classroomDTO.valueBeingChanged();
                        schoolList.get(schoolName)
                                .getClassroomByID(classId)
                                .setNumber(classNumber);
                  }

                  case "role_id", "channel_id" -> {
                        long id = (long) classroomDTO.valueBeingChanged();

                        if (update.contains("role"))
                        {
                              schoolList.get(schoolName)
                                      .getClassroomByID(classId)
                                      .setRoleID(id);
                        }
                        else
                        {
                              schoolList.get(schoolName)
                                      .getClassroomByID(classId)
                                      .setChannelID(id);
                        }
                  }

                  case "time" -> schoolList.get(schoolName)
                          .getClassroomByID(classId);

            }
            DatabaseUtils.updateClassroom(classroomDTO, schoolbot);
      }

      public void updateAssignment(CommandEvent event, DatabaseDTO assignmentDTO)
      {
            String update = assignmentDTO.updateColumn();
            Assignment assignment = (Assignment) assignmentDTO.objectBeingUpdated();
            var schoolBot = event.getSchoolbot();


            switch (update)
            {
                  case "name" -> {
                        String updatedElement = (String) assignmentDTO.valueBeingChanged();

                        assignment.getClassroom()
                                .getAssignmentByID(assignment.getId())
                                .setName(updatedElement);
                  }

                  case "description" -> {
                        String updatedElement = (String) assignmentDTO.valueBeingChanged();

                        assignment.getClassroom()
                                .getAssignmentByID(assignment.getId())
                                .setDescription(updatedElement);
                  }

                  case "points_possible" -> {
                        int updatedElement = (Integer) assignmentDTO.valueBeingChanged();

                        assignment.getClassroom()
                                .getAssignmentByID(assignment.getId())
                                .setPoints(updatedElement);
                  }

                  case "type" -> {
                        Assignment.AssignmentType type = (Assignment.AssignmentType) assignmentDTO.valueBeingChanged();

                        assignment.getClassroom()
                                .getAssignmentByID(assignment.getId())
                                .setType(type);
                  }

                  case "due_date" -> {
                        LocalDateTime ldt = (LocalDateTime) assignmentDTO.valueBeingChanged();

                        assignment.getClassroom()
                                .getAssignmentByID(assignment.getId())
                                .setDueDate(ldt);

                        assignment.setDueDate(ldt);

                        DatabaseUtils.removeAssignmentReminderByAssignment(schoolBot, assignment);
                        DatabaseUtils.addAssignmentReminder(schoolBot, assignment, List.of(1440, 60, 30, 10, 0));

                  }
            }

            DatabaseUtils.updateAssignment(assignmentDTO, event.getSchoolbot());

      }


      public void updateProfessor(CommandEvent event, DatabaseDTO databaseDTO)
      {
            String update = databaseDTO.updateColumn();
            Professor professor = (Professor) databaseDTO.objectBeingUpdated();
            String updatedElement = (String) databaseDTO.valueBeingChanged();

            switch (update)
            {
                  case "first_name" -> professor.getProfessorsSchool()
                          .getProfessorByID(professor.getId())
                          .setFirstName(updatedElement);

                  case "last_name" -> professor.getProfessorsSchool()
                          .getProfessorByID(professor.getId())
                          .setLastName(updatedElement);

                  case "email_prefix" -> professor.getProfessorsSchool()
                          .getProfessorByID(professor.getId())
                          .setEmailPrefix(updatedElement);
            }

            DatabaseUtils.updateProfessor(databaseDTO, event.getSchoolbot());
      }


      public void removeSchool(Schoolbot schoolbot, School school)
      {
            schoolList.remove(school.getName().toLowerCase());
            if (schoolbot.getJda().getRoleById(school.getRoleID()) != null)
            {
                  schoolbot.getJda().getRoleById(school.getRoleID()).delete().queue(
                          success ->
                                  LOGGER.info("Successfully deleted role for {}", school.getName()),

                          failure ->
                                  LOGGER.warn("Could not delete role for {} ", school.getName(), failure)
                  );
            }
            DatabaseUtils.removeSchool(schoolbot, school.getName());
      }


      public void addPittClass(CommandEvent event, Classroom classroom)
      {
            School school = classroom.getSchool();

            school.addPittClass(event, classroom);
            classrooms.add(classroom);

      }

      public void addClass(CommandEvent event, Classroom classroom)
      {
            School school = classroom.getSchool();

            school.addNormalClass(event, classroom);
            classrooms.add(classroom);

      }

      public boolean addProfessor(Schoolbot schoolbot, Professor professor)
      {
            int id = DatabaseUtils.addProfessor(schoolbot, professor);

            if (id == -1)
            {
                  return false;
            }
            professor.setId(id);
            professor.getProfessorsSchool().addProfessor(professor);
            return true;
      }

      public void removeProfessor(Schoolbot schoolbot, Professor professor)
      {
            professor.getProfessorsSchool().removeProfessor(professor);
            DatabaseUtils.removeProfessor(schoolbot, professor);
      }

      public void removeClassroom(CommandEvent event, Classroom classroom)
      {

            var jda = event.getJDA();
            classroom.getSchool().removeClass(classroom);
            classrooms.remove(classroom);

            if (classroom.getRoleID() != 0)
            {
                  if (jda.getRoleById(classroom.getRoleID()) != null)
                  {
                        jda.getRoleById(classroom.getRoleID()).delete().queue(success ->
                                        LOGGER.info("Successfully deleted role for {}", classroom.getName()),
                                failure ->
                                        LOGGER.warn("Could not delete role for {} ", classroom.getName(), failure)
                        );
                  }
            }

            if (classroom.getChannelID() != 0)
            {
                  if (jda.getTextChannelById(classroom.getChannelID()) != null)
                  {
                        jda.getTextChannelById(classroom.getChannelID()).delete().queue(
                                success ->
                                        LOGGER.info("Successfully deleted channel for {}", classroom.getName()),
                                failure ->
                                        LOGGER.warn("Could not delete class for {} ", classroom.getName(), failure)
                        );
                  }
            }


            DatabaseUtils.removeClassroom(event, classroom);
      }

      public void removeClassroom(long guildID, Classroom classroom, Schoolbot schoolbot)
      {
            var jda = schoolbot.getJda();
            classroom.getSchool().removeClass(classroom);
            classrooms.remove(classroom);

            if (classroom.getRoleID() != 0)
            {
                  if (jda.getRoleById(classroom.getRoleID()) != null)
                  {
                        jda.getRoleById(classroom.getRoleID()).delete().queue(success ->
                                        LOGGER.info("Successfully deleted role for {}", classroom.getName()),
                                failure ->
                                        LOGGER.warn("Could not delete role for {} ", classroom.getName(), failure)
                        );
                  }
            }

            if (classroom.getChannelID() != 0)
            {
                  if (jda.getTextChannelById(classroom.getChannelID()) != null)
                  {
                        jda.getTextChannelById(classroom.getChannelID()).delete().queue(
                                success ->
                                        LOGGER.info("Successfully deleted channel for {}", classroom.getName()),
                                failure ->
                                        LOGGER.warn("Could not delete class for {} ", classroom.getName(), failure)
                        );
                  }
            }


            DatabaseUtils.removeClassroom(schoolbot, classroom);
      }

      public List<Classroom> getAllClasses()
      {
            return classrooms;
      }


      public boolean containsSchool(String schoolName)
      {
            return schoolList.containsKey(schoolName.toLowerCase());
      }


      public List<Professor> getProfessorList(String schoolName)
      {
            String lowerCaseSchoolName = schoolName.toLowerCase();

            if (!schoolList.containsKey(lowerCaseSchoolName)) return Collections.emptyList();

            return schoolList.get(lowerCaseSchoolName).getProfessorList();
      }

      public void addAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            assignment.getClassroom().addAssignment(schoolbot, assignment);
      }

      public void removeAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            assignment.getClassroom().removeAssignment(schoolbot, assignment);
      }


      public boolean setGuildPrefix(String newGuildPrefix, CommandEvent event)
      {
            var oldPrefix = guildPrefix;
            this.guildPrefix = newGuildPrefix;

            boolean returnValue = DatabaseUtils.updatePrefix(newGuildPrefix, event);

            if (!returnValue)
            {
                  this.guildPrefix = oldPrefix;
            }
            return returnValue;
      }


      public String getGuildPrefix()
      {
            return guildPrefix;
      }

      public List<School> getSchoolList()
      {
            return new ArrayList<>(schoolList.values());
      }
}
