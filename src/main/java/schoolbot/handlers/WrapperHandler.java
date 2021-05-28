package schoolbot.handlers;

import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.commands.school.SchoolEdit;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.guild.GuildWrapper;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WrapperHandler
{
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final ConcurrentHashMap<Long, GuildWrapper> guildWrappers;
      private final Schoolbot schoolbot;

      public WrapperHandler(Schoolbot schoolbot)
      {
            this.guildWrappers = new ConcurrentHashMap<>();
            this.schoolbot = schoolbot;

      }

      public boolean addSchool(CommandEvent event, School school)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).addSchool(event, school);
            return true;
      }

      public List<School> getSchools(CommandEvent event)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getSchoolList();
      }

      public List<Classroom> getGuildsClasses(CommandEvent event)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getAllClasses(event);
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

      public School getSchool(CommandEvent event, String schoolName)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getSchool(schoolName);
      }

      public void updateSchool(CommandEvent event, SchoolEdit.SchoolUpdateDTO schoolUpdateDTO)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).updateSchool(event, schoolUpdateDTO);
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

            guildWrappers.get(guildID).removeClassroom(event, classroom);
      }


      public List<Page> getProfessorsAsPaginator(CommandEvent event, School school)
      {
            Schoolbot schoolbot = event.getSchoolbot();

            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            List<Page> pages = new ArrayList<>();
            List<Professor> professorList = getProfessors(event, school.getName().toLowerCase());


            int i = 1;

            for (Professor professor : professorList)
            {
                  pages.add(new Page
                          (PageType.EMBED,
                                  professor.getAsEmbedBuilder(schoolbot)
                                          .setFooter("Page " + i++ + "/" + professorList.size())
                                          .build()
                          ));
            }
            return pages;
      }

      private void guildCheck(long guildID)
      {

            guildWrappers.computeIfAbsent(
                    guildID,
                    (x) -> new GuildWrapper(DatabaseUtil.getSchools(schoolbot, x))
            );
      }
}
