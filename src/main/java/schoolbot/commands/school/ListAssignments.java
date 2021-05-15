package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListAssignments extends Command
{

      public ListAssignments()
      {
            super("description", "syntax", 0);
            addCalls("assignments");
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

            Member member = event.getMember();
            MessageChannel channel = event.getChannel();
            long textChanel = event.getTextChannel().getIdLong();

            if (args.size() == 0)
            {
                  if (!member.hasPermission(Permission.ADMINISTRATOR))
                  {
                        List<Classroom> classroomList = event.getGuildClasses();


                        Classroom classroom = Checks.messageSentFromClassChannel(event);

                        if (classroom == null)
                        {
                              // We are going to search the users roles because they didn't send the message from a textchannel that contains a class
                              List<Long> classRoles = classroomList
                                      .stream()
                                      .map(Classroom::getRoleID)
                                      .collect(Collectors.toList());

                              List<Long> validRoles = event.getMember()
                                      .getRoles()
                                      .stream()
                                      .map(Role::getIdLong)
                                      .filter(validRolez -> Collections.frequency(classRoles, validRolez) > 1)
                                      .collect(Collectors.toList());

                              List<Classroom> classrooms = classroomList
                                      .stream()
                                      .filter(classes -> Collections.frequency(validRoles, classes.getRoleID()) > 1)
                                      .filter(classes -> classes.getAssignments().size() > 0)
                                      .collect(Collectors.toList());

                              if (classrooms.isEmpty())
                              {
                                    Embed.error(event, "You dont have any roles that match any classes");
                                    return;
                              }

                              event.getAsPaginatorWithPageNumbers(classrooms);
                        }
                        else
                        {
                              List<Assignment> assignments = classroom.getAssignments();

                              if (assignments.isEmpty())
                              {
                                    Embed.error(event, "This class has no assignments!");
                              }
                              else if (assignments.size() == 1)
                              {
                                    event.sendMessage(assignments.get(0).getAsEmbed(event.getSchoolbot()));
                              }
                              else
                              {
                                    event.getAsPaginatorWithPageNumbers(assignments);
                              }
                        }


                  }
                  else
                  {
                        // Admin User
                        List<School> schoolList = event.getGuildSchools();

                        if (schoolList.isEmpty())
                        {
                              Embed.error(event, "%s has no schools associated with it", event.getGuild().getName());
                        }
                        else if (schoolList.size() == 1)
                        {
                              School school = schoolList.get(0);

                              event.sendMessage(school.getAsEmbed(event.getSchoolbot()));
                              event.sendMessageFormat("** %s ** was selected because this server only has one school", event.getGuild().getName());

                              event.getJDA().addEventListener(new ListAssignmentsStateMachine(event, schoolList, 1));
                        }
                        else
                        {
                              event.getJDA().addEventListener(new ListAssignmentsStateMachine(event, schoolList, 2));
                        }
                  }
            }

            else if (args.size() == 1)
            {

            }
      }

      public static class ListAssignmentsStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private final List<School> schools;
            private final int state;


            public ListAssignmentsStateMachine(CommandEvent event, List<School> school, int stateToSwitchTo)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.schools = school;
                  this.state = stateToSwitchTo;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  switch (state)
                  {
                        case 1 -> {

                        }

                        case 2 -> {

                        }

                        case 3 -> {

                        }
                  }
            }
      }
}