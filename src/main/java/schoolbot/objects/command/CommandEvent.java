package schoolbot.objects.command;


import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Constants;
import schoolbot.Schoolbot;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.Paginatable;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandEvent
{
      Logger LOGGER = LoggerFactory.getLogger(this.getClass());
      private final GuildMessageReceivedEvent event;
      private final Schoolbot schoolbot;
      private final Command command;
      private final List<String> args;
      private final ExecutorService commandThreadPool;

      public CommandEvent(GuildMessageReceivedEvent event, Command command, List<String> args, Schoolbot schoolbot, ExecutorService commandThreadPool)
      {
            this.event = event;
            this.command = command;
            this.args = args;
            this.schoolbot = schoolbot;
            this.commandThreadPool = commandThreadPool;
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

      public ExecutorService getCommandThreadPool()
      {
            return commandThreadPool;
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
            getChannel().sendMessageEmbeds(embed).queue();
      }

      public void sendMessage(EmbedBuilder embedBuilder)
      {
            getChannel().sendMessageEmbeds(
                    embedBuilder.setColor(Constants.DEFAULT_EMBED_COLOR)
                            .setTimestamp(Instant.now()).build())
                    .queue();
      }

      public void sendMessage(String message, Object... args)
      {
            getChannel().sendMessage(String.format(message, args)).queue();
      }


      public void sendMessage(EmbedBuilder embedBuilder, Color color)
      {
            getTextChannel().sendMessageEmbeds(embedBuilder
                    .setColor(color)
                    .setTimestamp(Instant.now())
                    .build()
            ).queue();
      }

      public <T extends Paginatable> void sendAsNormalPaginator(List<T> list)
      {
            List<Page> pages = new ArrayList<>();

            for (T obj : list)
            {
                  pages.add(new Page(obj.getAsEmbed(schoolbot)));
            }

            getChannel().sendMessageEmbeds(
                    (MessageEmbed) pages.get(0).getContent()
            ).queue(success -> Pages.paginate(success, pages));
      }

      public void sendAsPaginator(List<MessageEmbed> embeds)
      {
            List<Page> pages = new ArrayList<>();

            for (var embed : embeds)
            {
                  pages.add(new Page(embed));
            }
            getChannel().sendMessageEmbeds(
                    (MessageEmbed) pages.get(0).getContent()
            ).queue(success -> Pages.paginate(success, pages));
      }


      public <T extends Paginatable> void sendAsPaginatorWithPageNumbers(List<T> list)
      {
            // This is just in case I call a list with pages when theres only one page...
            if (list.size() == 1)
            {
                  this.sendMessage(list.get(0).getAsEmbed(schoolbot));
                  return;
            }

            List<MessageEmbed> embeds = new ArrayList<>();

            int i = 1;
            for (T obj : list)
            {
                  embeds.add(obj.getAsEmbedBuilder(schoolbot)
                          .setFooter("Page (" + i++ + "/" + list.size() + ")")
                          .build()
                  );
            }


            sendAsPaginator(embeds);
      }


      public void getProfessorsAsPaginator(School school)
      {
            List<MessageEmbed> embeds = schoolbot.getWrapperHandler().getProfessorsAsPaginator(this, school);


            sendAsPaginator(embeds);
      }


      public void sendSelfDeletingMessage(String message)
      {
            getChannel().sendMessage(message).queue(deleting -> deleting.delete().queueAfter(10, TimeUnit.SECONDS));
      }

      public List<School> getGuildSchools()
      {
            return schoolbot.getWrapperHandler().getSchools(this);
      }

      /**
       * @param schoolName Name of school
       * @return List of Professors within the school
       */
      public List<Professor> getSchoolsProfessors(String schoolName)
      {
            return schoolbot.getWrapperHandler().getProfessors(this, schoolName);
      }


      public boolean schoolExist(String schoolName)
      {
            return schoolbot.getWrapperHandler().schoolCheck(this, schoolName);
      }

      public void addSchool(School school)
      {
            schoolbot.getWrapperHandler().addSchool(this, school);
      }

      public void updateSchool(DatabaseDTO schoolUpdateDTO)
      {
            schoolbot.getWrapperHandler().updateSchool(this, schoolUpdateDTO);
      }

      public void updateProfessor(DatabaseDTO professorUpdate)
      {
            schoolbot.getWrapperHandler().updateProfessor(this, professorUpdate);
      }

      public void updateAssignment(DatabaseDTO assignmentUpdate)
      {
            schoolbot.getWrapperHandler().updateAssignment(this, assignmentUpdate);
      }

      public void updateClassroom(DatabaseDTO classroomUpdateDTO)
      {
            schoolbot.getWrapperHandler().updateClassroom(this, classroomUpdateDTO);
      }

      public School getSchool(String schoolName)
      {
            return schoolbot.getWrapperHandler().getSchool(this, schoolName);
      }

      public void addPittClass(Classroom classroom)
      {
            schoolbot.getWrapperHandler().addPittClass(this, classroom);
      }

      public void removeSchool(School school)
      {
            schoolbot.getWrapperHandler().removeSchool(this, school);
      }

      public void removeProfessor(Professor professor)
      {
            schoolbot.getWrapperHandler().removeProfessor(this, professor);
      }

      public void removeClass(Classroom classroom)
      {
            schoolbot.getWrapperHandler().removeClassroom(this, classroom);
      }

      public boolean addProfessor(Professor professor)
      {
            return schoolbot.getWrapperHandler().addProfessor(this, professor);
      }

      public void addAssignment(Assignment assignment)
      {
            schoolbot.getWrapperHandler().addAssignment(this, assignment);
      }

      public void removeAssignment(Assignment assignment)
      {
            schoolbot.getWrapperHandler().removeAssignment(this, assignment);
      }

      public List<Classroom> getGuildClasses()
      {
            return schoolbot.getWrapperHandler().getGuildsClasses(this);
      }

      public boolean isDeveloper()
      {
            return event.getAuthor().getIdLong() == Constants.GENIUS_OWNER_ID;
      }


}
