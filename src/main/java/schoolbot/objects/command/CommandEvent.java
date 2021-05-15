package schoolbot.objects.command;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.objects.misc.Paginatable;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandEvent
{
      Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final GuildMessageReceivedEvent event;
      private final Schoolbot schoolbot;
      private final Command command;
      private final List<String> args;

      public CommandEvent(GuildMessageReceivedEvent event, Command command, List<String> args, Schoolbot schoolbot)
      {
            this.event = event;
            this.command = command;
            this.args = args;
            this.schoolbot = schoolbot;
      }

      public GuildMessageReceivedEvent getEvent()
      {
            return event;
      }

      public Schoolbot getSchoolbot()
      {
            return schoolbot;
      }

      public Command getCommand()
      {
            return command;
      }

      public List<String> getArgs()
      {
            return args;
      }

      public Guild getGuild()
      {
            return event.getGuild();
      }

      public TextChannel getTextChannel()
      {
            return event.getChannel();
      }

      public User getUser()
      {
            return event.getAuthor();
      }

      public Member getMember()
      {
            return event.getMember();
      }

      public MessageChannel getChannel()
      {
            return event.getChannel();
      }

      public JDA getJDA()
      {
            return event.getJDA();
      }

      public Message getMessage()
      {
            return event.getMessage();
      }

      public boolean memberPermissionCheck(List<Permission> list)
      {
            return (getMember() != null && getMember().hasPermission(event.getChannel(), list));
      }

      public boolean selfPermissionCheck(List<Permission> list)
      {
            return event.getGuild().getSelfMember().hasPermission(list);
      }

      public boolean selfPermissionCheck(Permission... permissions)
      {
            return event.getGuild().getSelfMember().hasPermission(permissions);
      }

      public void sendMessage(String message)
      {
            getChannel().sendMessage(message).queue();
      }

      public void sendMessageFormat(String message, Object... args)
      {
            getChannel().sendMessageFormat(message, args).queue();
      }


      public void sendMessage(MessageEmbed embed)
      {
            getChannel().sendMessage(embed).queue();
      }

      public void sendMessage(EmbedBuilder embedBuilder)
      {
            getChannel().sendMessage(
                    embedBuilder.setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR)
                            .setTimestamp(Instant.now()).build())
                    .queue();
      }

      public void sendMessage(String message, Object... args)
      {
            getChannel().sendMessage(String.format(message, args)).queue();
      }


      public void sendMessage(EmbedBuilder embedBuilder, Color color)
      {
            getChannel().sendMessage(
                    embedBuilder.setColor(color)
                            .setTimestamp(Instant.now()).build())
                    .queue();
      }

      public <T extends Paginatable> void getAsPaginator(List<T> list)
      {
            List<Page> pages = new ArrayList<>();

            for (T obj : list)
            {
                  pages.add(new Page(PageType.EMBED, obj.getAsEmbed(schoolbot)));
            }

            getChannel().sendMessage(
                    (MessageEmbed)
                            pages.get(0).getContent()
            ).queue(success ->
                    Pages.paginate(success, pages)
            );
      }

      public <T extends Paginatable> void getAsPaginatorWithPageNumbers(List<T> list)
      {
            // This is just in case I call a list with pages when theres only one page...
            if (list.size() == 1)
            {
                  getAsPaginator(list);
                  return;
            }

            List<Page> pages = new ArrayList<>();
            int i = 1;
            for (T obj : list)
            {
                  pages.add(new Page(PageType.EMBED, obj.getAsEmbedBuilder(schoolbot)
                          .setFooter("Page " + i++ + "/" + list.size())
                          .build()
                  ));
            }


            getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(
                    success ->
                    {
                          Pages.paginate(success, pages);
                    }
            );
      }


      public void getProfessorsAsPaginator(School school)
      {
            List<Page> pages = schoolbot.getWrapperHandler().getProfessorsAsPaginator(this, school);

            getChannel().sendMessage(
                    (MessageEmbed)
                            pages.get(0).getContent()
            ).queue(success ->
                    Pages.paginate(success, pages)
            );
      }


      public void sendSelfDeletingMessage(String message)
      {
            getChannel().sendMessage(message).queue(deleting -> deleting.delete().queueAfter(10, TimeUnit.SECONDS));
      }

      public List<School> getGuildSchools()
      {
            return schoolbot.getWrapperHandler().getSchools(this);
      }

      public List<Professor> getSchoolsProfessors(String schoolName)
      {
            return schoolbot.getWrapperHandler().getProfessors(this, schoolName);
      }


      public boolean schoolExist(String schoolName)
      {
            return schoolbot.getWrapperHandler().schoolCheck(this, schoolName);
      }

      public void addSchool(CommandEvent event, School school)
      {
            schoolbot.getWrapperHandler().addSchool(event, school);
      }

      public School getSchool(CommandEvent event, String schoolName)
      {
            return schoolbot.getWrapperHandler().getSchool(event, schoolName);
      }

      public void addPittClass(CommandEvent event, Classroom classroom)
      {
            schoolbot.getWrapperHandler().addPittClass(this, classroom);
      }

      public void removeSchool(CommandEvent event, School school)
      {
            schoolbot.getWrapperHandler().removeSchool(this, school);
      }

      public void removeProfessor(CommandEvent event, Professor professor)
      {
            schoolbot.getWrapperHandler().removeProfessor(event, professor);
      }

      public void removeClass(CommandEvent event, Classroom classroom)
      {
            schoolbot.getWrapperHandler().removeClassroom(event, classroom);
      }


      public boolean addProfessor(CommandEvent event, Professor professor)
      {
            return schoolbot.getWrapperHandler().addProfessor(event, professor);
      }

      public void addAssignment(CommandEvent event, Assignment assignment)
      {
            schoolbot.getWrapperHandler().addAssignment(event, assignment);
      }

      public List<Classroom> getGuildClasses()
      {
            return schoolbot.getWrapperHandler().getGuildsClasses(this);
      }

      public boolean isDeveloper()
      {
            return event.getAuthor().getIdLong() == SchoolbotConstants.GENIUS_OWNER_ID;
      }


}
