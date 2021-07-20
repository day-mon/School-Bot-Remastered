package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.DatabaseDTO;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ClassroomEdit extends Command
{
      private boolean inSelectionMenu = false;

      public ClassroomEdit(Command parent)
      {
            super(parent, "Edits a classroom", "[none]", 0);
            addSelfPermissions(Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var schoolList = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getClassroomList().isEmpty())
                    .collect(Collectors.toList());
            var jda = event.getJDA();
            var classroom = Checks.messageSentFromClassChannel(values);
            values.setSchoolList(schoolList);


            if (classroom != null)
            {
                  EmbedUtils.information(event, """
                          **%s** has been selected because you sent a message from this channel.
                                                                
                          Please pick a selection from the menu
                                                                
                          If you would like to exit at any time (including now) please type '**exit**' or '**stop**'
                                                                
                          """, classroom.getName());
                  sendMenu(values);
                  values.setState(3);
                  jda.addEventListener(new ClassroomEditStateMachine(values));
                  return;
            }

            var processedSchoolList = Processor.processGenericList(values, schoolList, School.class);

            if (processedSchoolList == 1)
            {
                  var processedClassList = Processor.processGenericList(values, values.getClassroomList(), Classroom.class);

                  if (processedClassList == 1)
                  {

                        EmbedUtils.information(event, """
                                **%s** has been selected because you this is the only class available
                                                                      
                                Please pick a selection from the menu
                                                                      
                                If you would like to exit at any time (including now) please type '**exit**' or '**stop**'
                                                                      
                                """, classroom.getName());
                        sendMenu(values);
                        values.setState(3);
                  }
                  else if (processedClassList == 2)
                  {
                        values.setState(2);
                  }
            }
            jda.addEventListener(new ClassroomEditStateMachine(values));

      }

      private class ClassroomEditStateMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;

            public ClassroomEditStateMachine(@NotNull StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);

                  if (!requirementsMet) return;

                  var state = values.getState();
                  switch (state)
                  {
                        case 1 -> {
                              var successfulOption = Processor.validateMessage(values, values.getSchoolList());

                              if (!successfulOption)
                              {
                                    return;
                              }

                              EmbedUtils.information(event, "** %s ** has successfully been chosen", values.getSchool().getName());
                              var processedList = Processor.processGenericList(values, values.getClassroomList(), Classroom.class);

                              if (processedList == 1)
                              {
                                    sendMenu(values);
                              }

                        }

                        case 2 -> {

                              if (inSelectionMenu)
                              {
                                    EmbedUtils.warn(event, "You need to select an option from the menu");
                                    return;
                              }

                              var successfulOption = Processor.validateMessage(values, values.getClassroomList());
                              values.setState(2);

                              if (!successfulOption)
                              {
                                    return;
                              }

                              EmbedUtils.information(event, "** %s ** has successfully been chosen", values.getClassroom().getName());

                              sendMenu(values);
                        }

                        case 3 -> {
                              if (inSelectionMenu)
                              {
                                    EmbedUtils.warn(event, "Please choose a option from the menu. I do not need a message from you!");
                                    return;
                              }
                              evaluateMessage(values);
                        }
                  }

            }
      }

      private void evaluateChoice(StateMachineValues values, SelectionMenuEvent selectionMenuEvent)
      {
            var optionChosen = selectionMenuEvent.getValues().get(0);
            var classroom = values.getClassroom();
            var event = values.getCommandEvent();
            var jda = values.getJda();
            var guild = event.getGuild();
            var machine = values.getMachine();
            var autofilled = classroom.isAutoFilled();
            var prefix = event.getGuildPrefix();
            var className = classroom.getName().toLowerCase().replaceAll("\\s", "-");

            switch (optionChosen)
            {
                  case "name" -> {
                        if (autofilled)
                        {
                              EmbedUtils.error(event, "You cannot edit the name of a class that was autofilled!");
                              jda.removeEventListener(machine);
                              return;
                        }

                        values.setUpdateColumn("name");
                        EmbedUtils.information(event, "** %s ** is the current class name.. What would you like to change it to?", classroom.getName());
                  }

                  case "description" -> {
                        EmbedUtils.information(event, "What would you like to change %s's descriptions to", classroom.getName());
                        values.setUpdateColumn("description");
                  }

                  case "professor" -> {
                        if (autofilled)
                        {
                              EmbedUtils.error(event, "You cannot edit the name of a class that was auto filled! I will now stop listening for responses.");
                              jda.removeEventListener(machine);
                              return;
                        }

                        var professorList = values.getProfessorList()
                                .stream()
                                .filter(professor -> !professor.equals(classroom.getProfessor()))
                                .collect(Collectors.toList());

                        var processedList = Processor.processGenericList(values, professorList, Professor.class);

                        if (processedList == 0)
                        {
                              EmbedUtils.error(event, "Sorry %s has no available professor for you to switch to. Please use the %sprofessor add to make a new professor", classroom.getName(), prefix);
                              jda.removeEventListener(machine);
                              return;
                        }

                        if (processedList == 1)
                        {
                              var professor = values.getProfessor();
                              jda.removeEventListener(machine);
                              EmbedUtils.confirmation(event, "**%s** is the only professor available to switch. Would you like to switch to them?", (messageReactionAddEvent) ->
                              {
                                    var reactionEmote = messageReactionAddEvent.getReactionEmote().getName();

                                    if (reactionEmote.equals(Emoji.CROSS_MARK.getUnicode()))
                                    {
                                          EmbedUtils.warn(event, "Okay I am no longer listening on!");
                                    }
                                    else if (reactionEmote.equals(Emoji.WHITE_CHECK_MARK.getUnicode()))
                                    {
                                          var commandEvent = values.getCommandEvent();

                                          commandEvent.updateClassroom(new DatabaseDTO(classroom, "professor_id", professor.getId()));
                                          EmbedUtils.success(event, "Professor successfully changed to %s", professor.getFullName());
                                    }
                              }, professor.getFullName());
                              return;
                        }
                        values.setUpdateColumn("professor_id");
                  }

                  case "time" -> {
                        event.sendMessage("""
                                Please give me the time now.
                                                                      
                                I will need the day and the time.

                                ```
                                Day Mappings:
                                Mo = Monday
                                Tu = Tuesday
                                We = Wednesday
                                Th = Thursday
                                Fr = Friday
                                                                      
                                Format:
                                Day(s) <start time> - <end time>
                                                                      
                                Example:
                                MoWeFri 1:00PM - 1:50PM This class is on Monday Wednesday and Friday from 1:00pm to 1:50pm```
                                """);

                        values.setUpdateColumn("time");
                  }

                  case "startDate", "endDate" -> {
                        if (autofilled)
                        {
                              EmbedUtils.error(event, "You cannot edit the name of a class that was auto filled! I will now stop listening for responses.");
                              jda.removeEventListener(machine);
                              return;
                        }

                        LocalDate date;
                        String dateName;


                        if (optionChosen.equals("startDate"))
                        {
                              dateName = "start";
                              date = classroom.getStartDate();
                        }
                        else
                        {
                              dateName = "end";
                              date = classroom.getEndDate();
                        }


                        EmbedUtils.information(event, """
                                **%s** is the current %s date
                                                                      
                                Please start by giving me the date. **Format: M/dd/yyyy**
                                Examples include: `5/1/2021` and `9/12/2021`
                                """, StringUtils.formatDate(date), dateName);

                        values.setUpdateColumn(dateName + "_date");
                  }

                  case "number" -> {
                        if (autofilled)
                        {
                              EmbedUtils.error(event, "You cannot edit the name of a class that was auto filled! I will now stop listening for responses.");
                              jda.removeEventListener(machine);
                              return;
                        }

                        event.getChannel().sendMessageFormat("** %d ** is the class number. What would you like to change it to?", classroom.getNumber()).queue();
                        values.setUpdateColumn("number");
                  }

                  case "role" -> {
                        var role = jda.getRoleById(classroom.getRoleID());

                        List<SelectOption> selectOptionList = List.of(
                                SelectOption.of("Create a Role for you", "create"),
                                SelectOption.of("Assign one yourself", "assign")
                        );

                        values.setUpdateColumn("role_id");


                        if (role == null)
                        {
                              inSelectionMenu = true;

                              event.sendMenuAndAwait("This class has no role assigned to it. Here are your options", selectOptionList, (selectionMenuEvent1) ->
                              {
                                    var itemChosen = selectionMenuEvent1.getValues().get(0);
                                    Role roleCreated;

                                    if (itemChosen.equals("create"))
                                    {
                                          roleCreated = guild.createRole()
                                                  .setName(className.toLowerCase().replaceAll("\\s", "-"))
                                                  .setColor(new Random().nextInt(0xFFFFFF))
                                                  .complete();

                                          EmbedUtils.success(event, "Role successfully changed to: %s", roleCreated.getAsMention());
                                          event.updateClassroom(new DatabaseDTO(classroom, values.getUpdateColumn(), roleCreated.getIdLong()));
                                          jda.removeEventListener(machine);
                                    }
                                    else if (itemChosen.equals("assign"))
                                    {
                                          EmbedUtils.information(event, "Please mention the role you would like to use as your new role");
                                          inSelectionMenu = false;
                                          values.setState(3);
                                    }
                              });
                              return;
                        }
                        else if (role != null)
                        {
                              EmbedUtils.information(event, "Please mention the role you would like to use as your new role");
                        }
                  }

                  case "channel" -> {
                        var textChannel = jda.getTextChannelById(classroom.getChannelID());

                        values.setUpdateColumn("channel_id");


                        if (textChannel == null)
                        {
                              inSelectionMenu = true;

                              event.sendMenuAndAwait("This class has no TextChannel assigned to it. Here are your options", List.of(
                                      SelectOption.of("Create one for you", "create"),
                                      SelectOption.of("Assign one yourself", "assign")),

                                      (selectionMenuEvent1) ->
                                      {

                                            System.out.println("state at beginning -> " + values.getState());
                                            var itemChosen = selectionMenuEvent1.getValues().get(0);
                                            TextChannel channel;

                                            if (itemChosen.equals("create"))
                                            {
                                                  var potentialRole = jda.getRoleById(classroom.getRoleID());

                                                  if (potentialRole == null)
                                                  {
                                                        EmbedUtils.warn(event, "This class does not have a role assigned to it. When setting up the channel I will only allow admins to view it.");

                                                        channel = guild.createTextChannel(className)
                                                                .setName(className)
                                                                .addRolePermissionOverride(guild.getIdLong(), 0L, Permission.ALL_GUILD_PERMISSIONS)
                                                                .complete();
                                                  }
                                                  else
                                                  {
                                                        channel = guild.createTextChannel(className)
                                                                .setName(className)
                                                                .addRolePermissionOverride(potentialRole.getIdLong(), Permission.ALL_GUILD_PERMISSIONS, 0L)
                                                                .addRolePermissionOverride(guild.getIdLong(), 0L, Permission.ALL_GUILD_PERMISSIONS)
                                                                .complete();
                                                  }
                                                  event.updateClassroom(new DatabaseDTO(classroom, values.getUpdateColumn(), channel.getIdLong()));
                                                  EmbedUtils.success(event, "TextChannel successfully changed to: %s", channel.getAsMention());
                                                  jda.removeEventListener(machine);
                                            }
                                            else if (itemChosen.equals("assign"))
                                            {
                                                  EmbedUtils.information(event, "Please mention the channel you would like to use as your new channel");
                                                  inSelectionMenu = false;
                                                  values.setState(3);
                                            }
                                      });
                              return;
                        }
                        else if (textChannel != null)
                        {
                              EmbedUtils.information(event, "Please mention the channel you would like to use as your new channel");
                        }
                  }
            }
            values.setState(3);
            inSelectionMenu = false;

      }


      private static void evaluateMessage(StateMachineValues values)
      {
            var message = values.getMessageReceivedEvent().getMessage().getContentRaw();
            var updateColumn = values.getUpdateColumn();
            var messageEvent = values.getMessageReceivedEvent();
            var event = values.getCommandEvent();
            var classroom = values.getClassroom();
            var jda = values.getJda();


            switch (updateColumn)
            {
                  case "name" -> {
                        var length = message.length();

                        if (length >= Constants.MAX_EMBED_TITLE)
                        {
                              EmbedUtils.error(event, "Your class title was %d characters long. It cannot exceed 256 characters", length);
                              return;
                        }

                        String newNames = message.toLowerCase().replace("\\s", "-");


                        var potentialRole = jda.getRoleById(classroom.getRoleID());

                        if (potentialRole != null)
                        {
                              potentialRole.getManager()
                                      .setName(newNames)
                                      .queue(null, failure ->
                                              event.getSchoolbot().getLogger().warn("Error occurred while trying to rename the role {} in the during the class edit process", newNames));
                        }

                        var potentialChannel = jda.getTextChannelById(classroom.getChannelID());

                        if (potentialChannel != null)
                        {
                              potentialChannel.getManager()
                                      .setName(newNames)
                                      .queue(null, failure ->
                                              event.getSchoolbot().getLogger().warn("Error occurred while trying to rename the channel {} in the during the class edit process", newNames));
                        }

                        event.updateClassroom(new DatabaseDTO(classroom, updateColumn, message));
                        EmbedUtils.success(event, "Name successfully changed to: %s", message);
                        values.getJda().removeEventListener(values.getMachine());

                  }

                  case "description" -> {
                        int length = message.length();

                        if (length >= Constants.MAX_FIELD_VALUE)
                        {
                              EmbedUtils.error(event, "Your class description was %d characters long. It cannot exceed 1024 characters", length);
                              return;
                        }

                        event.updateClassroom(new DatabaseDTO(classroom, updateColumn, message));
                        EmbedUtils.success(event, "Description changed successfully!");
                        jda.removeEventListener(values.getMachine());


                  }

                  case "professor_id" -> {
                        var validMessage = Processor.validateMessage(values, values.getProfessorList());

                        if (!validMessage)
                        {
                              return;
                        }

                        event.updateClassroom(new DatabaseDTO(classroom, updateColumn, values.getProfessor().getId()));
                        EmbedUtils.success(event, "Professor changed successfully to %s", values.getProfessor().getFullName());
                        jda.removeEventListener(values.getMachine());

                  }

                  case "start_date", "end_date" -> {
                        var date = Checks.checkValidDate(message);

                        if (date == null)
                        {
                              EmbedUtils.error(event, "** %s ** is not a valid date. Please try again", message);
                              return;

                        }

                        if (updateColumn.equals("start_date"))
                        {
                              var classroomEndDate = classroom.getEndDate();

                              if (!classroomEndDate.minusYears(1).isBefore(date))
                              {
                                    EmbedUtils.error(event, """
                                                     ** %s ** is more than a year away. Most are not a year long.
                                                    `If this is the case please contact: damon#9999`
                                                     """,
                                            StringUtils.formatDate(date));
                                    return;
                              }

                              if (!date.isBefore(classroomEndDate))
                              {
                                    EmbedUtils.error(event, "**%s** is either before or ends on the same day as the class starts. Please choose a separate date", StringUtils.formatDate(date));
                                    return;
                              }

                              classroom.setStartDate(Date.valueOf(date));
                        }
                        else
                        {
                              var classroomStartDate = classroom.getStartDate();

                              if (!classroomStartDate.plusYears(1).isAfter(date))
                              {
                                    EmbedUtils.error(event, """
                                             ** %s ** is more than a year away. Most are not a year long.
                                            `If this is the case please contact: damon#9999`
                                             """, StringUtils.formatDate(date));
                                    return;
                              }

                              if (!date.isAfter(classroomStartDate))
                              {
                                    EmbedUtils.error(event, "**%s** is either before or start on the same day as the class ends. Please choose a separate date", StringUtils.formatDate(date));
                                    return;
                              }

                              classroom.setEndDate(Date.valueOf(date));
                        }

                        var evaluation = Parser.classTime(event.getSchoolbot(), classroom.getTime(), classroom);

                        if (!evaluation)
                        {
                              event.sendMessage("Please try again!");
                              return;
                        }

                        var time = updateColumn.contains("start") ? classroom.getStartDateWithTime().toLocalTime() : classroom.getEndDateWithTime().toLocalTime();

                        event.updateClassroom(new DatabaseDTO(classroom, updateColumn, LocalDateTime.of(date, time)));
                        EmbedUtils.success(event, "Date Successfully changed!");
                        jda.removeEventListener(values.getMachine());

                  }

                  case "number" -> {
                        var number = Checks.isNum(message);

                        if (number == -1)
                        {
                              EmbedUtils.error(event, "** %s ** is not a number. Please try again!", message);
                              return;
                        }

                        event.updateClassroom(new DatabaseDTO(classroom, updateColumn, number));
                        EmbedUtils.success(event, "Class Number successfully changed to: %s", number);
                        jda.removeEventListener(values.getMachine());

                  }

                  case "role_id" -> {
                        var msg = messageEvent.getMessage();
                        int mentionedRoles = msg.getMentionedRoles().size();


                        if (mentionedRoles != 0)
                        {
                              var role = msg.getMentionedRoles().get(0);
                              if (mentionedRoles > 1)
                              {
                                    EmbedUtils.warn(event, """
                                            You have mentioned ** %d ** roles.
                                            I will use the first one you mentioned, which is %s
                                            """, mentionedRoles, role.getAsMention());
                              }
                              EmbedUtils.success(event, "Role successfully changed to: %s", role.getAsMention());
                              event.updateClassroom(new DatabaseDTO(classroom, updateColumn, role.getIdLong()));
                              values.getJda().removeEventListener(values.getMachine());
                        }
                        else
                        {
                              EmbedUtils.error(event, "You did not mention any roles. Please mention a role to continue!");
                        }

                  }

                  case "channel_id" -> {
                        var msg = messageEvent.getMessage();
                        int mentionedChannels = msg.getMentionedChannels().size();


                        if (mentionedChannels != 0)
                        {
                              var textChannel = msg.getMentionedChannels().get(0);
                              if (mentionedChannels > 1)
                              {
                                    EmbedUtils.warn(event, """
                                            You have mentioned **%d** channels.
                                            I will use the first one you mentioned, which is %s
                                            """, mentionedChannels, textChannel.getAsMention());
                              }
                              EmbedUtils.success(event, "Channel successfully changed to: %s", textChannel.getAsMention());
                              event.updateClassroom(new DatabaseDTO(classroom, updateColumn, textChannel.getIdLong()));
                              jda.removeEventListener(values.getMachine());

                        }
                        else
                        {
                              EmbedUtils.error(event, "You did not mention any text channels. Please try again and mention a text channel!");
                        }

                  }

                  case "time" -> {
                        var evaluation = Parser.classTime(event.getSchoolbot(), message, classroom);

                        if (!evaluation)
                        {
                              event.sendMessage("Please try again!");
                              return;
                        }


                        event.updateClassroom(new DatabaseDTO(classroom, updateColumn, message));
                        EmbedUtils.success(event, "Time Successfully changed!");
                        jda.removeEventListener(values.getMachine());

                  }
            }
      }


      private void sendMenu(StateMachineValues values)
      {
            inSelectionMenu = true;
            final List<SelectOption> selectOptionList = List.of(
                    SelectOption.of("Name", "name"),
                    SelectOption.of("Description", "description"),
                    SelectOption.of("Professor", "professor"),
                    SelectOption.of("Time", "time"),
                    SelectOption.of("Start Date", "startDate"),
                    SelectOption.of("End Date", "endDate"),
                    SelectOption.of("Number", "number"),
                    SelectOption.of("Role", "role"),
                    SelectOption.of("Channel", "channel")
            );


            var stringToSend = values.getClassroom().getName().length() >= 100 ? "Your class has been chosen, What attribute would you like to update" : String.format("%s has been chosen, What attribute would you like to update", values.getClassroom().getName());

            values.getCommandEvent().sendMenuAndAwait(stringToSend, selectOptionList, (selectionMenuEvent) ->
                    evaluateChoice(values, selectionMenuEvent));
      }
}