package schoolbot.handlers;

import net.dv8tion.jda.api.entities.MessageEmbed;
import schoolbot.Schoolbot;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.guild.GuildWrapper;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WrapperHandler
{
      private final ConcurrentHashMap<Long, GuildWrapper> guildWrappers;
      private final Schoolbot schoolbot;

      public WrapperHandler(Schoolbot schoolbot)
      {
            this.guildWrappers = new ConcurrentHashMap<>();
            this.schoolbot = schoolbot;

      }

      public void addSchool(CommandEvent event, School school)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).addSchool(event, school);
      }

      public List<School> getSchools(CommandEvent event)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getSchoolList();
      }

      public List<School> getSchools(long guildId)
      {
            guildCheck(guildId);
            return guildWrappers.get(guildId).getSchoolList();
      }

      public List<Classroom> getGuildsClasses(CommandEvent event)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getAllClasses();
      }


      public boolean schoolCheck(CommandEvent event, String schoolName)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).containsSchool(schoolName);
      }

      public List<Professor> getProfessors(CommandEvent event, String schoolName)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getProfessorList(schoolName);
      }

      public void addPittClass(CommandEvent event, Classroom classroom)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);

            guildWrappers.get(guildID).addPittClass(event, classroom);
      }

      public void addClass(CommandEvent event, Classroom classroom)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);

            guildWrappers.get(guildID).addClass(event, classroom);
      }

      public List<Classroom> getClasses(long guildID)
      {
            guildCheck(guildID);

            return guildWrappers.get(guildID).getAllClasses();
      }

      public void addAssignment(CommandEvent event, Assignment assignment)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);

            guildWrappers.get(guildID).addAssignment(schoolbot, assignment);
      }

      public void removeAssignment(CommandEvent event, Assignment assignment)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);

            guildWrappers.get(guildID).removeAssignment(schoolbot, assignment);
      }


      public void removeAssignment(long guildID, Assignment assignment)
      {
            guildCheck(guildID);

            // This looks like I am doing extra stuff, but the assignment passed in isnt the same object reference variable
            Assignment newAssign = guildWrappers.get(guildID)
                    .getAllClasses()
                    .stream()
                    .filter(classroom -> classroom.equals(assignment.getClassroom()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Assignment Exist in Database but not in cache"))
                    .getAssignmentByID(assignment.getId());

            guildWrappers.get(guildID).removeAssignment(schoolbot, newAssign);
      }

      public void removeGuildFromCache(long guildId)
      {
            guildWrappers.remove(guildId);
      }

      public School getSchool(CommandEvent event, String schoolName)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getSchool(schoolName);
      }

      public void updateSchool(CommandEvent event, DatabaseDTO schoolUpdateDTO)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).updateSchool(event, schoolUpdateDTO);
      }


      public void updateProfessor(CommandEvent event, DatabaseDTO schoolUpdateDTO)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).updateProfessor(event, schoolUpdateDTO);
      }

      public void updateAssignment(CommandEvent event, DatabaseDTO assignmentUpdate)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).updateAssignment(event, assignmentUpdate);
      }

      public void updateClassroom(CommandEvent event, DatabaseDTO classroomUpdateDTO)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).updateClassroom(event, classroomUpdateDTO);
      }


      public void removeSchool(CommandEvent event, School school)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            guildWrappers.get(guildID).removeSchool(schoolbot, school);
      }

      public boolean addProfessor(CommandEvent event, Professor professor)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            return guildWrappers.get(guildID).addProfessor(schoolbot, professor);
      }

      public void removeProfessor(CommandEvent event, Professor professor)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            guildWrappers.get(guildID).removeProfessor(schoolbot, professor);
      }

      public void removeClassroom(CommandEvent event, Classroom classroom)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            guildWrappers.get(guildID).removeClassroom(classroom, event.getSchoolbot());
      }

      public void removeClassroom(Classroom classroom, Schoolbot schoolbot)
      {
            var guildId = classroom.getGuildID();


            guildCheck(guildId);

            var newClass = guildWrappers.get(guildId)
                    .getAllClasses()
                    .stream()
                    .filter(classroom::equals)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Classroom Exist in Database but not in cache"));

            guildWrappers.get(guildId).removeClassroom(newClass, schoolbot);
      }




      private void guildCheck(long guildID)
      {

            guildWrappers.computeIfAbsent(
                    guildID,
                    (x) -> new GuildWrapper(DatabaseUtils.loadGuild(schoolbot, x))
            );
      }

      public String fetchGuildPrefix(long guildId)
      {
            guildCheck(guildId);

            return guildWrappers.get(guildId).getGuildPrefix();
      }

      public boolean assignGuildPrefix(CommandEvent event, String prefix)
      {
            var guildId = event.getGuild().getIdLong();
            guildCheck(guildId);

            return guildWrappers.get(guildId).setGuildPrefix(prefix, event);
      }
}
