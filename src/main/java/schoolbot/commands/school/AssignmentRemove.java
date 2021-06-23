package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AssignmentRemove extends Command
{
      /**
       * @param parent Parent command of [Assignment]
       */
      public AssignmentRemove(Command parent)
      {
            super(parent, "Removes an assignment from a class", "[none]", 0);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();

            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getClassroomList().isEmpty())
                    .collect(Collectors.toList());
            List<Assignment> assignmentList = Collections.emptyList();
            Assignment assignment = null;
            Classroom classroom = Checks.messageSentFromClassChannel(event);

            int stateToGoto = 1;
            if (classroom != null)
            {
                  event.sendMessage("""
                          ** %s ** has been selected because it you sent it from this channel
                          Please give me the name of the assignment!
                          """, classroom.getName());

                  assignmentList = classroom.getAssignments();

                  if (assignmentList.isEmpty())
                  {
                        Embed.error(event, "** %s ** has no assignments.", classroom.getName());
                        return;
                  }
                  else if (assignmentList.size() == 1)
                  {
                        assignment = assignmentList.get(0);

                        event.sendMessage(assignment.getAsEmbed(event.getSchoolbot()));
                        event.sendMessage("** %s ** is the only assignment.. Would you like to delete it?", assignment.getName());
                        stateToGoto = 5;
                  }
                  else
                  {
                        event.sendAsPaginatorWithPageNumbers(assignmentList);
                        event.sendMessage("Please select the assignment by page number");
                        stateToGoto = 4;

                  }
            }
            else
            {
                  if (member.hasPermission(Permission.ADMINISTRATOR))
                  {
                        if (schools.isEmpty())
                        {
                              Embed.error(event, "This server does not have any school associated with it!");
                              return;
                        }
                        else if (schools.size() == 1)
                        {
                              classroom = new Classroom();
                              classroom.setSchool(schools.get(0));
                              event.getChannel().sendMessageFormat("** %s ** has been selected because there is only one school in this server", classroom.getSchool().getName()).queue();
                              event.getChannel().sendMessage("Would you like to continue?").queue();
                              stateToGoto = 2;
                        }
                        else
                        {
                              event.sendMessage("Please choose the School ID of the school you want to add the assignment to ");
                              event.sendAsPaginatorWithPageNumbers(schools);
                        }
                  }
                  else
                  {
                        List<Long> validRoles = Checks.validRoleCheck(event);

                        List<Classroom> classrooms = event.getGuildClasses()
                                .stream()
                                .filter(classes -> Collections.frequency(validRoles, classes.getRoleID()) > 1)
                                .filter(classes -> !classes.getAssignments().isEmpty())
                                .collect(Collectors.toList());

                        if (classrooms.isEmpty())
                        {
                              Embed.error(event, "You do not have any roles that indicate you attend any classes");
                              return;
                        }
                        else if (classrooms.size() == 1)
                        {
                              classroom = classrooms.get(0);

                              assignmentList = classroom.getAssignments();

                              if (assignmentList.isEmpty())
                              {
                                    Embed.error(event, "** %s ** has no assignments.", classroom.getName());
                                    return;
                              }
                              else if (assignmentList.size() == 1)
                              {
                                    assignment = assignmentList.get(0);

                                    event.sendMessage(assignment.getAsEmbed(event.getSchoolbot()));
                                    event.sendMessage("** %s ** is the only assignment.. Would you like to delete it?", assignment.getName());
                                    stateToGoto = 1;
                              }
                              else
                              {
                                    event.sendAsPaginatorWithPageNumbers(assignmentList);
                                    event.sendMessage("Please select the assignment by page number");
                              }
                        }
                        else
                        {
                              event.sendAsPaginatorWithPageNumbers(classrooms);
                              event.sendMessage("Please choose the page number of the class you want to remove an assignment from");
                        }
                  }
            }
            event.getJDA().addEventListener(new AssignmentRemoveMachine(event, schools, assignmentList, classroom, assignment, stateToGoto));
      }

      public static class AssignmentRemoveMachine extends ListenerAdapter
      {
            private final CommandEvent commandEvent;
            private final long authorID, channelID;
            private final List<School> schools;
            private List<Classroom> classroomList;
            private List<Assignment> assignmentList;
            private School school;
            private Classroom classroom;
            private Assignment assignment;
            private int state;

            public AssignmentRemoveMachine(CommandEvent commandEvent, List<School> schools, List<Assignment> assignmentList, Classroom classroom, Assignment assignment, int state)
            {
                  this.commandEvent = commandEvent;
                  this.channelID = commandEvent.getChannel().getIdLong();
                  this.authorID = commandEvent.getUser().getIdLong();
                  this.state = state;
                  this.schools = schools;
                  this.classroom = classroom;
                  this.assignment = assignment;
                  this.assignmentList = assignmentList;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().getIdLong() != authorID) return;
                  if (event.getChannel().getIdLong() != channelID) return;

                  MessageChannel channel = event.getChannel();
                  String message = event.getMessage().getContentRaw();
                  JDA jda = event.getJDA();
                  Guild guild = event.getGuild();


                  if (message.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Aborting...").queue();
                        jda.removeEventListener(this);
                        return;
                  }

                  switch (state)
                  {
                        case 1 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, """
                                            ** %s ** is not a number
                                            Please Enter a number
                                            """, message);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(message);

                              if (!Checks.between(pageNumber, schools.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the school ids...", message);
                                    return;
                              }

                              classroom.setSchool(schools.get(pageNumber - 1));
                              Embed.success(event, "** %s ** successfully selected", classroom.getSchool().getName());
                              channel.sendMessage("Would you like to continue?").queue();
                              state = 2;
                        }


                        case 2 -> {
                              if (!message.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay goodbye").queue();
                                    jda.removeEventListener(this);
                                    return;
                              }

                              classroomList = commandEvent.getSchool(classroom.getSchool().getName()).getClassroomList()
                                      .stream()
                                      .filter(classroom -> !classroom.getAssignments().isEmpty())
                                      .collect(Collectors.toList());

                              if (classroomList.isEmpty())
                              {
                                    Embed.error(event, "** %s ** does not have any classes associated with it", guild.getName());
                                    jda.removeEventListener(this);
                              }
                              else if (classroomList.size() == 1)
                              {
                                    Embed.success(event, "** %s ** has been selected automatically because you only have one class associated with you!", classroomList.get(0).getName());
                                    channel.sendMessageFormat("""
                                                
                                            """, Emoji.SMILEY_FACE.getAsChat()
                                    ).queue();
                                    this.classroom = classroomList.get(0);
                                    state = 4;
                              }
                              else
                              {
                                    commandEvent.sendAsPaginatorWithPageNumbers(classroomList);
                                    channel.sendMessage("Please give me the page number of the class you want to remove the assignment from").queue();
                              }
                        }

                        case 3 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", message);
                                    return;
                              }

                              int index = Integer.parseInt(message) - 1;

                              if (!Checks.between(index + 1, classroomList.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the class ids...", message);
                                    event.getJDA().removeEventListener(this);
                                    return;
                              }
                              this.classroom = classroomList.get(index);

                              Embed.success(event, "** %s ** has successfully been selected", this.classroom.getName());

                              this.assignmentList = classroom.getAssignments();
                              if (assignmentList.isEmpty())
                              {
                                    Embed.error(event, "** %s ** has no assignments.", classroom.getName());
                                    return;
                              }
                              else if (assignmentList.size() == 1)
                              {
                                    Assignment assignment = assignmentList.get(0);

                                    channel.sendMessage(assignment.getAsEmbed(commandEvent.getSchoolbot())).queue();
                                    channel.sendMessageFormat("** %s ** is the only assignment.. Would you like to delete it?", assignment.getName()).queue();
                                    state = 5;
                              }
                              else
                              {
                                    commandEvent.sendAsPaginatorWithPageNumbers(assignmentList);
                                    channel.sendMessage("Please select the assignment by page number").queue();
                              }
                              state = 4;
                        }

                        case 4 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(event, """
                                            ** %s ** is not a number
                                            Please Enter a number
                                            """, message);
                                    return;
                              }

                              int pageNumber = Integer.parseInt(message);

                              if (!Checks.between(pageNumber, assignmentList.size()))
                              {
                                    Embed.error(event, "** %s ** was not one of the assignment page numbers...", message);
                                    return;
                              }

                              this.assignment = assignmentList.get(pageNumber - 1);
                              channel.sendMessageFormat("Are you sure you want to delete ** %s ** ?", this.assignment.getName()).queue();
                              state = 5;
                        }

                        case 5 -> {
                              if (message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y"))
                              {
                                    commandEvent.removeAssignment(assignment);
                                    Embed.success(event, "Removed [** %s **] successfully", this.assignment.getName());
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
