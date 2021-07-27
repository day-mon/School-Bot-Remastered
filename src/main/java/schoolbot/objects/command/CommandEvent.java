package schoolbot.objects.command;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Constants;
import schoolbot.Schoolbot;
import schoolbot.interactions.ButtonPaginator;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.interfaces.Paginatable;
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
import java.util.function.Consumer;

public class CommandEvent
{
      private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
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
                    .queue(null, failure -> LOGGER.error("Failure whilst sending Embed"));
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
            ).queue(null, failure -> LOGGER.error("Failure whilst sending Embed"));

      }

      public void bPaginator(List<MessageEmbed> embeds)
      {
            ButtonPaginator s = new ButtonPaginator.Builder()
                    .setJDA(getJDA())
                    .setEmbeds(embeds)
                    .setTimeout(60)
                    .setChannel(getChannel())
                    .setWaiter(schoolbot.getEventWaiter())
                    .build();
            s.paginate();
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


            bPaginator(embeds);
      }


      public void sendMenuAndAwait(String placeHolder, List<SelectOption> selectOptions, Consumer<SelectionMenuEvent> consumer)
      {
            var stringToSend = placeHolder.length() >= 100 ? "Choose an option from the menu" : placeHolder;

            SelectionMenu menu = SelectionMenu.create("menu:class")
                    .setPlaceholder(stringToSend)
                    .setRequiredRange(1, 1)
                    .addOptions(selectOptions)
                    .build();


            getChannel().sendMessage("Please select a option from the menu")
                    .setActionRows(ActionRow.of(menu))
                    .queue(message ->
                    {
                          var eventWaiter = schoolbot.getEventWaiter();

                          eventWaiter.waitForEvent(SelectionMenuEvent.class,
                                  selectionMenuEvent ->
                                  {
                                        if (selectionMenuEvent.getMessageIdLong() != message.getIdLong()) return false;
                                        if (getMember().getIdLong() != selectionMenuEvent.getUser().getIdLong())
                                              return false;
                                        selectionMenuEvent.deferEdit().queue();
                                        return true;
                                  }, consumer);
                    });
      }


      public void sendSelfDeletingMessage(String message)
      {
            getChannel().sendMessage(message).queue(deleting -> deleting.delete().queueAfter(10, TimeUnit.SECONDS));
      }

      public void sendSelfDeletingMessageFormat(String message, Object... args)
      {
            getChannel().sendMessage(String.format(message, args)).queue(deleting -> deleting.delete().queueAfter(10, TimeUnit.SECONDS));
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

      public void addClass(Classroom classroom)
      {
            schoolbot.getWrapperHandler().addClass(this, classroom);
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


      public String getGuildPrefix()
      {
            var guildId = event.getGuild().getIdLong();
            return schoolbot.getWrapperHandler().fetchGuildPrefix(guildId);
      }

      public boolean assignPrefix(String prefix)
      {
            return schoolbot.getWrapperHandler().assignGuildPrefix(this, prefix);
      }
}