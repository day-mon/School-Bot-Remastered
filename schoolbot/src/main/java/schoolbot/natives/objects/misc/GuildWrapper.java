package schoolbot.natives.objects.misc;

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


      public GuildWrapper(long guildID, Map<String, School> schoolList)
      {
            this.guildID = guildID;
            this.schoolList = schoolList;
      }


      /*
            This is meant to serve kinda as a cache..
            Each Guild will have there own wrapper so I can easily list, add, edit, and remove schools without having to make many database connections
            Things like ListSchools, ListProfessor, ListAssignments, and ListClasses should not have database calls.. Not really needed and slow
       */


      public boolean addSchool(Schoolbot schoolbot, School school)
      {
            String lowerCaseSchoolName = school.getSchoolName().toLowerCase();

            if (schoolList.containsKey(lowerCaseSchoolName)) return false;
            schoolList.put(lowerCaseSchoolName, school);
            DatabaseUtil.addSchool(schoolbot, school);
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

      }

      public void addProfessor(Schoolbot schoolbot, Professor professor)
      {
            professor.getProfessorsSchool().addProfessor(professor);
            DatabaseUtil.addProfessor(schoolbot, professor);
      }

      public void removeProfessor(Schoolbot schoolbot, Professor professor)
      {
            professor.getProfessorsSchool().removeProfessor(professor);
            DatabaseUtil.removeProfessor(schoolbot, professor);
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
