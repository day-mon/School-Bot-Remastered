package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.StateMachine;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;
import schoolbot.util.Processor;

import java.util.List;
import java.util.stream.Collectors;

public class SchoolRemove extends Command
{
      public SchoolRemove(Command parent)
      {
            super(parent, "Removes a school given the name", "[school name]", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var jda = event.getJDA();
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> school.getClassroomList().isEmpty())
                    .filter(school -> school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());
            values.setSchoolList(schools);

            var processedList = Processor.processGenericList(values, schools, School.class);

            if (processedList == 0)
            {
                  return;
            }

            if (processedList == 1)
            {
                  event.sendMessage("This is the only school available to delete would you like to delete it?");

                  var school = values.getSchool();
                  values.setState(2);
            }

            jda.addEventListener(new SchoolRemoveStateMachine(values));

      }

      public static class SchoolRemoveStateMachine extends ListenerAdapter implements StateMachine
      {
            private final StateMachineValues values;


            public SchoolRemoveStateMachine(StateMachineValues values)
            {
                  this.values = values;
            }


            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  var channel = event.getChannel();
                  var message = event.getMessage().getContentRaw();

                  if (!Checks.eventMeetsPrerequisites(values))
                  {
                        return;
                  }


                  int state = values.getState();
                  // TOdo: Fix this

                  switch (state)
                  {
                        case 1 -> {
                              var schools = values.getSchoolList();
                              var success = Processor.validateMessage(values, schools);

                              if (!success)
                              {
                                    return;
                              }


                              var school = values.getSchool();
                              channel.sendMessageFormat("Are you sure you want to remove [ ** %s **]", school.getName()).queue();
                        }

                        case 2 -> {
                              var school = values.getSchool();
                              var commandEvent = values.getCommandEvent();
                              if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y"))
                              {
                                    commandEvent.removeSchool(school);
                                    Embed.success(event, "Removed [** %s **] successfully", school.getName());
                                    event.getJDA().removeEventListener(this);
                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("nah"))
                              {
                                    channel.sendMessage("Okay.. aborting..").queue();
                                    event.getJDA().removeEventListener(this);
                              }
                              else
                              {
                                    Embed.error(event, "[ ** %s ** ] is not a valid respond.. I will need a **Yes** OR a **No**", message);
                              }
                        }
                  }
            }
      }
}
