package schoolbot.natives.objects.misc;

import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;

import java.util.ArrayList;
import java.util.List;

public class GuildWrapper
{
      private long guildID;
      private List<School> schoolList;
      private List<Classroom> classrooms;
      private List<Professor> professors;


      public GuildWrapper(long guildID)
      {
            this.guildID = guildID;
            this.classrooms = new ArrayList<>();
            this.schoolList = new ArrayList<>();
            this.professors = new ArrayList<>();
      }


      /*
            This is meant to serve kinda as a cache..
            Each Guild will have there own wrapper so I can easily list, add, edit, and remove schools without having to make many database connections
            Things like ListSchools, ListProfessor, ListAssignments, and ListClasses should not have database calls.. Not really needed and slow
       */


      public void addSchool(School school)
      {
            schoolList.add(school);

            // Add to database here..

      }

      public void addClass(Classroom classroom)
      {
            classrooms.add(classroom);
            // You'd probably want a add professor here as well.

            // Add to database here..
      }

      public void addProfessor(Professor professor)
      {
            professors.add(professor);

            // add to database here
      }


      public long getGuildID()
      {
            return guildID;
      }

      public void setGuildID(long guildID)
      {
            this.guildID = guildID;
      }

      public List<School> getSchoolList()
      {
            return schoolList;
      }

      public void setSchoolList(List<School> schoolList)
      {
            this.schoolList = schoolList;
      }

      public List<Classroom> getClassrooms()
      {
            return classrooms;
      }

      public void setClassrooms(List<Classroom> classrooms)
      {
            this.classrooms = classrooms;
      }

      public List<Professor> getProfessors()
      {
            return professors;
      }

      public void setProfessors(List<Professor> professors)
      {
            this.professors = professors;
      }
}
