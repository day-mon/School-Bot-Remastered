package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.StateMachine;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class ClassroomRemove extends Command
{

      public ClassroomRemove(Command parent)
      {
            super(parent, "Removes a class from a school", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES);
            addFlags(CommandFlag.DATABASE, CommandFlag.STATE_MACHINE_COMMAND);

      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();

            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(School::hasClasses)
                    .collect(Collectors.toList());
            values.setSchoolList(schools);


            var schoolSuccess = Processor.processGenericList(values, schools, School.class);

            if (schoolSuccess == 0)
            {
                  return;
            }
            else if (schoolSuccess == 1)
            {
                  event.sendMessage("This is the only school available with deletable classes.");
                  List<Classroom> classrooms = event.getGuildClasses()
                          .stream()
                          .filter(classroom -> classroom.getAssignments().isEmpty())
                          .collect(Collectors.toList());
                  values.setClassroomList(classrooms);

                  var classSuccess = Processor.processGenericList(values, classrooms, Classroom.class);

                  if (classSuccess == 1)
                  {
                        var classroom = values.getClassroom();
                        EmbedUtils.confirmation(event, "Are you sure you want to remove **%s**", (messageReactionAddEvent1) ->
                        {
                              var reactionEmote = messageReactionAddEvent1.getReactionEmote().getName();

                              if (reactionEmote.equals(Emoji.CROSS_MARK.getUnicode()))
                              {
                                    event.sendMessage("Okay aborting!");
                              }
                              else if (reactionEmote.equals(Emoji.WHITE_CHECK_MARK.getUnicode()))
                              {
                                    values.getCommandEvent().removeClass(classroom);
                                    EmbedUtils.success(event, "Removed **%s** successfully", classroom.getName());
                              }
                        }, classroom.getName());
                        return;
                  }
                  else if (classSuccess == 2)
                  {
                        values.setState(2);
                  }
            }

            // no need to set state to one because its defaulted to one.


            jda.addEventListener(new ClassroomRemoveStateMachine(values));

      }

      private static class ClassroomRemoveStateMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;


            public ClassroomRemoveStateMachine(@NotNull StateMachineValues values)
            {
                  values.setMachine(this);
                  this.values = values;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  values.setMessageReceivedEvent(event);
                  var requirementsMet = Checks.eventMeetsPrerequisites(values);
                  var channel = event.getChannel();
                  var jda = event.getJDA();

                  int state = values.getState();

                  if (!requirementsMet)
                  {
                        return;
                  }

                  String message = event.getMessage().getContentRaw();


                  switch (state)
                  {
                        case 1 -> {
                              var success = Processor.validateMessage(values, values.getSchoolList());

                              if (!success)
                              {
                                    return;
                              }

                              var classroomList = values.getSchool()
                                      .getClassroomList()
                                      .stream()
                                      .filter(clazzroom -> !clazzroom.hasAssignments())
                                      .collect(Collectors.toList());

                              var processedList = Processor.processGenericList(values, classroomList, Classroom.class);

                              if (processedList == 0)
                              {
                                    jda.removeEventListener(this);
                                    return;
                              }

                              if (processedList == 1)
                              {
                                    var classroom = values.getClassroom();

                                    EmbedUtils.information(event, "%s is the only class available to delete. Would you like to delete this class?", classroom.getName());
                                    values.setState(3);
                                    return;
                              }


                              var school = values.getSchool();


                              EmbedUtils.success(event, "** %s ** has been selected", school.getName());

                        }


                        case 2 -> {

                              var success = Processor.validateMessage(values, values.getClassroomList());


                              if (!success) return;

                              var classroom = values.getClassroom();

                              EmbedUtils.confirmation(values.getCommandEvent(), "Are you sure you want to remove **%s**", (messageReactionAddEvent) ->
                              {
                                    var reactionEmote = messageReactionAddEvent.getReactionEmote().getName();

                                    if (reactionEmote.equals(Emoji.CROSS_MARK.getUnicode()))
                                    {
                                          channel.sendMessage("Okay.. aborting..").queue();
                                    }
                                    else if (reactionEmote.equals(Emoji.WHITE_CHECK_MARK.getUnicode()))
                                    {
                                          values.getCommandEvent().removeClass(classroom);
                                          EmbedUtils.success(event, "Removed **%s** successfully", classroom.getName());
                                    }

                              }, classroom.getName());
                              jda.removeEventListener(this);
                        }
                  }
            }
      }
}
