package schoolbot.handlers;

import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.misc.GuildWrapper;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WrapperHandler
{
      private ConcurrentHashMap<Long, GuildWrapper> guildWrappers;
      private Schoolbot schoolbot;

      public WrapperHandler(Schoolbot schoolbot)
      {
            this.guildWrappers = new ConcurrentHashMap<>();
            this.schoolbot = schoolbot;
      }

      public boolean addSchool(CommandEvent event, School school)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            guildWrappers.get(guildID).addSchool(schoolbot, school);
            return true;
      }

      public List<School> getSchools(CommandEvent event)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getSchoolList();
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

      public School getSchool(CommandEvent event, String schoolName)
      {
            long guildID = event.getGuild().getIdLong();

            guildCheck(guildID);
            return guildWrappers.get(guildID).getSchool(schoolName);
      }

      public void removeSchool(CommandEvent event, School school)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            guildWrappers.get(guildID).removeSchool(schoolbot, school);
      }

      public void addProfessor(CommandEvent event, Professor professor)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            guildWrappers.get(guildID).addProfessor(schoolbot, professor);
      }

      public void removeProfessor(CommandEvent event, Professor professor)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            guildWrappers.get(guildID).removeProfessor(schoolbot, professor);
      }


      public List<Page> getSchoolsAsPaginator(CommandEvent event)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            List<Page> pages = new ArrayList<>();
            List<School> schools = getSchools(event);
            int i = 1;

            for (School school : schools)
            {
                  pages.add(new Page
                          (PageType.EMBED,
                                  school.getAsEmbedBuilder(schoolbot)
                                          .setFooter("Page " + i++ + "/" + schools.size())
                                          .build()
                          ));
            }
            return pages;

      }

      public List<Page> getProfessorsAsPaginator(CommandEvent event, School school)
      {
            long guildID = event.getGuild().getIdLong();
            guildCheck(guildID);

            List<Page> pages = new ArrayList<>();
            List<Professor> professorList = getProfessors(event, school.getSchoolName().toLowerCase());

            int i = 1;

            for (Professor professor : professorList)
            {
                  pages.add(new Page
                          (PageType.EMBED,
                                  professor.getAsEmbedBuilder()
                                          .setFooter("Page " + i++ + "/" + professorList.size())
                                          .build()
                          ));
            }
            return pages;
      }

      private void guildCheck(long guildID)
      {
            if (guildWrappers.containsKey(guildID)) return;

            guildWrappers.put(guildID,
                    new GuildWrapper(
                            guildID,
                            DatabaseUtil.getSchools(schoolbot, guildID)
                    ));
      }


}
