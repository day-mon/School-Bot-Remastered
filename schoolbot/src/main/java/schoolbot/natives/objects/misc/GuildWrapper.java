package schoolbot.natives.objects.misc;

import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.Assignment;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuildWrapper
{
      private long guildID;
      private Map<String, School> schoolList;
      // This is a really bad and I will fix it later;
      private List<Classroom> classrooms;


      public GuildWrapper(@NotNull DatabaseUtil.Data data)
      {
            this.guildID = data.getGuildID();
            this.schoolList = data.getSchoolMap();
            this.classrooms = data.getClassrooms();
      }

      /*
            This is meant to serve kinda as a cache..
            Each Guild will have there own wrapper so I can easily list, add, edit, and remove schools without having to make many database connections
            Things like ListSchools, ListProfessor, ListAssignments, and ListClasses should not have database calls.. Not really needed and slow
       */

      public boolean addSchool(CommandEvent event, School school)
      {
            String lowerCaseSchoolName = school.getSchoolName().toLowerCase();
            if (schoolList.containsKey(lowerCaseSchoolName)) return false;

            int id = DatabaseUtil.addSchool(event, school);
            if (id == -1) return false;

            school.setSchoolID(id);
            schoolList.put(lowerCaseSchoolName, school);

            return true;
      }

      public School getSchool(String schoolName)
      {
            return schoolList.get(schoolName.toLowerCase());
      }

      public void removeSchool(Schoolbot schoolbot, School school)
      {
            schoolList.remove(school.getSchoolName().toLowerCase());
            if (schoolbot.getJda().getRoleById(school.getRoleID()) != null)
            {
                  schoolbot.getJda().getRoleById(school.getRoleID()).delete().queue();
            }
            DatabaseUtil.removeSchool(schoolbot, school.getSchoolName());
      }


      public void addPittClass(CommandEvent event, Classroom classroom)
      {
            School school = classroom.getSchool();

            school.addPittClass(event, classroom);
            classrooms.add(classroom);

      }

      public boolean addProfessor(Schoolbot schoolbot, Professor professor)
      {


            int id = DatabaseUtil.addProfessor(schoolbot, professor);

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
            DatabaseUtil.removeProfessor(schoolbot, professor);
      }

      public List<Classroom> getAllClasses(CommandEvent event)
      {
            return classrooms;
      }


      public long getGuildID()
      {
            return guildID;
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

            String lowerCaseSchoolName = assignment.getName().toLowerCase();

            assignment.getClassroom().addAssignment(schoolbot, assignment);
      }


      public List<School> getSchoolList()
      {
            return new ArrayList<School>(schoolList.values());
      }
}
