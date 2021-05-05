package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.objects.school.Assignment;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignmentAdd extends Command
{

      /**
       * @param parent
       */
      public AssignmentAdd(Command parent)
      {
            super(parent, " ", " ", 0);
            addFlags(CommandFlag.DATABASE);

      }

      /**
       * @param event Arguments sent to the command.
       */
      // TODO: Start this command by 05/02/2021
      /*
            Potential Issues;
                  - A user could have multiple school roles.
       */
      @Override
      public void run(CommandEvent event)
      {
            List<School> schools = DatabaseUtil.getSchools(event.getSchoolbot(), event.getGuild().getIdLong());

            Map<Integer, School> validIDs = new HashMap<>();
            Classroom classroom = null;
            int stateToGoto = 1;

            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR))
            {
                  event.sendMessage("To add an assignment you can either start with your **class number** or the **class name** ");
            }


            if (schools.isEmpty())
            {
                  Embed.error(event, "This server does not have any school associated with it!");
                  event.getJDA().removeEventListener(this);
                  return;
            }
            else if (schools.size() == 1)
            {
                  classroom = new Classroom();
                  classroom.setSchool(schools.get(0));
                  event.getChannel().sendMessageFormat("** %s ** has been selected because there is only one school in this server", classroom.getSchool().getSchoolName()).queue();
                  event.getChannel().sendMessage("Would you like to continue?").queue();
                  stateToGoto = 2;
            }
            else
            {
                  event.sendMessage("Please choose the School ID of the school you want to add the assignment to ");
                  ArrayList<Page> pages = new ArrayList<>();

                  for (School school : schools)
                  {
                        validIDs.put(school.getSchoolID(), school);
                        pages.add(new Page(PageType.EMBED, school.getAsEmbed(event.getSchoolbot())));
                  }

                  event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent())
                          .queue(success -> Pages.paginate(success, pages));
            }


            event.getJDA().addEventListener(new AssignmentAddStateMachine(event.getSchoolbot(), event.getChannel(), event.getUser(), validIDs, classroom, stateToGoto));


      }

      public static class AssignmentAddStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private int state;
            private Schoolbot schoolbot;
            private Assignment assignment;
            private Classroom classroom;

            private List<School> schoolList;
            private List<Classroom> classroomList;

            private Map<Integer, School> validSchoolIDs;
            private Map<Integer, Classroom> validClassIDs;


            public AssignmentAddStateMachine(Schoolbot schoolbot, MessageChannel channel, User author, Map<Integer, School> validIDs, Classroom classroom, int state)
            {
                  this.channelID = channel.getIdLong();
                  this.authorID = author.getIdLong();
                  this.schoolbot = schoolbot;
                  this.schoolList = new ArrayList<>();
                  this.classroomList = new ArrayList<>();
                  this.validSchoolIDs = validIDs;
                  this.classroom = classroom == null ? new Classroom() : classroom;
                  this.validClassIDs = new HashMap<>();
                  this.assignment = new Assignment();
                  this.state = state;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().isBot()) return;
                  if (event.getChannel().getIdLong() != channelID) return;
                  if (event.getAuthor().getIdLong() != authorID) return;

                  JDA jda = event.getJDA();
                  MessageChannel channel = event.getChannel();
                  String content = event.getMessage().getContentRaw();
                  Member user = event.getMember();
                  Guild guild = event.getGuild();

                  if (content.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Okay aborting..").queue();
                        jda.removeEventListener(this);
                        return;
                  }


                  switch (state)
                  {

                        case 1 -> {
                              if (!Checks.isNumber(content))
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", content);
                                    return;
                              }

                              int schoolID = Integer.parseInt(content);

                              if (!validSchoolIDs.containsKey(schoolID))
                              {
                                    Embed.error(event, "** %s ** was not one of the school ids...", content);
                                    return;
                              }

                              classroom.setSchoolID(schoolID);
                              classroom.setSchool(validSchoolIDs.get(schoolID));
                              Embed.success(event, "** %s ** successfully selected", classroom.getSchool().getSchoolName());
                              state = 2;
                              return;
                        }

                        case 2 -> {
                              if (!content.toLowerCase().contains("yes"))
                              {
                                    channel.sendMessage("Okay goodbye").queue();
                                    jda.removeEventListener(this);
                                    return;
                              }

                              if (user.hasPermission(Permission.ADMINISTRATOR))
                              {
                                    classroomList = DatabaseUtil.getClassesWithinSchool(schoolbot, guild.getIdLong(), classroom.getSchool().getSchoolID());
                                    ArrayList<Page> pages = new ArrayList<>();

                                    for (Classroom classroom : classroomList)
                                    {
                                          validClassIDs.put(classroom.getId(), classroom);
                                          pages.add(new Page(PageType.EMBED, classroom.getAsEmbedShort(schoolbot)));
                                    }

                                    if (classroomList.isEmpty())
                                    {
                                          Embed.error(event, "** %s ** does not have any classes associated with it", guild.getName());
                                          jda.removeEventListener(this);
                                    }
                                    else if (classroomList.size() == 1)
                                    {
                                          Embed.success(event, "** %s ** has been selected automatically because you only have one class associated with you!", classroomList.get(0).getClassName());
                                          channel.sendMessageFormat("""
                                                  Now that we have all that sorted the fun stuff can start %s
                                                  Im going to start by asking for your assignment name
                                                  """, Emoji.SMILEY_FACE.getAsChat()
                                          ).queue();
                                          state = 4;
                                          return;
                                    }
                                    else
                                    {
                                          channel.sendMessage("Please choose a class id from the following").queue();
                                          channel.sendMessage((MessageEmbed) pages.get(0).getContent())
                                                  .queue(success -> Pages.paginate(success, pages));
                                          state = 3;
                                          return;
                                    }
                              }
                              // first check all users roles...
                              else
                              {
                                    List<Long> roleIDs = user.getRoles().stream()
                                            .map(Role::getIdLong)
                                            .collect(Collectors.toList());
                                    List<Classroom> classes = new ArrayList<>();


                                    // This is actually horrifically slow and not really scalable could use a hashmap or store these before hand .
                                    for (Classroom classroom : DatabaseUtil.getClassesWithinSchool(schoolbot, event.getGuild().getIdLong(), classroom.getSchoolID()))
                                    {
                                          if (roleIDs.contains(classroom.getRoleID()))
                                          {
                                                classes.add(classroom);
                                          }
                                    }

                                    if (classes.isEmpty())
                                    {
                                          Embed.error(event, "You do not have any roles associated with any classes");
                                          jda.removeEventListener(this);
                                          return;
                                    }
                                    else if (classes.size() == 1)
                                    {
                                          Embed.success(event, "** %s ** has been selected automatically because you only have one class associated with you!", classes.get(0).getClassName());
                                          channel.sendMessageFormat("""
                                                  Now that we have all that sorted the fun stuff can start %s
                                                  Im going to start by asking for your assignment name
                                                  """, Emoji.SMILEY_FACE.getAsChat()
                                          ).queue();
                                          state = 4;
                                          return;
                                    }
                                    else
                                    {
                                          channel.sendMessage("You have more than one class associated with you.. Please give me the class id").queue();
                                          ArrayList<Page> pages = new ArrayList<>();

                                          for (Classroom classroom : classes)
                                          {
                                                validClassIDs.put(classroom.getId(), classroom);
                                                pages.add(new Page(PageType.EMBED, classroom.getAsEmbedShort(schoolbot)));

                                          }


                                          channel.sendMessage((MessageEmbed) pages.get(0).getContent())
                                                  .queue(success -> Pages.paginate(success, pages));
                                          state = 3;
                                          return;
                                    }


                              }
                        }
                        case 3 -> {
                              if (!Checks.isNumber(content))
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", content);
                                    return;
                              }

                              int classID = Integer.parseInt(content);

                              if (!validClassIDs.containsKey(classID))
                              {
                                    Embed.error(event, "** %s ** was not one of the class ids...", content);
                                    event.getJDA().removeEventListener(this);
                                    return;
                              }
                              this.classroom = validClassIDs.get(classID);
                              Embed.success(event, "** %s ** has successfully been selected", this.classroom.getClassName());
                              channel.sendMessageFormat("""
                                      Now that we have all that sorted the fun stuff can start %s
                                      Im going to start by asking for your assignment name
                                      """, Emoji.SMILEY_FACE.getAsChat()
                              ).queue();
                              state = 4;
                              return;
                        }

                        case 4 -> {
                              assignment.setProfessorID(classroom.getProfessorID());
                              assignment.setName(content);

                              Embed.success(event, "** %s ** has successfully been added as Assignment name..", assignment.getName());
                              channel.sendMessageFormat("Please give me a small description about the assignment. You can change it later so if you wanna speed through this its fine %s", Emoji.SMILEY_FACE.getAsChat()).queue();
                              state = 5;
                              return;
                        }

                        case 5 -> {
                              assignment.setDescription(content);
                              Embed.success(event, "Description has successfully been added as Assignment name..");
                              channel.sendMessage("Okay got it im going to need the point amount for the assignment.. If you don't know just put 'idk' or 0").queue();
                              state = 6;
                              return;
                        }

                        case 6 -> {
                              if (!Checks.isNumber(content) || content.toLowerCase().contains("idk"))
                              {
                                    Embed.error(event, "** %s ** is not a number.. try again!", content);
                                    state = 6;
                                    return;
                              }

                              int points = content.toLowerCase().contains("idk") ? 0 : Integer.parseInt(content);

                              assignment.setPoints(points);
                              Embed.success(event, "** %d ** has been set as ** %s ** point amount", points, assignment.getName());
                              channel.sendMessage("""
                                      Now I will need the type of assignment it is 
                                      ```
                                      Valid Answers
                                      1. Exam
                                      2. Quiz
                                      3. Extra Credit
                                      4. Homework
                                      ```
                                      """).queue();
                              state = 7;
                              return;
                        }

                        case 7 -> {
                              String message = content.toLowerCase();

                              if (message.contains("exam") || message.contains("1"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.EXAM);
                              }
                              else if (message.contains("homework") || message.contains("work") || message.contains("4"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.HOMEWORK);
                              }
                              else if (message.contains("quiz") || message.contains("2"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.QUIZ);
                              }
                              else if (message.contains("extra") || message.contains("credit") || message.contains("3"))
                              {
                                    assignment.setAssignmentType(Assignment.AssignmentType.EXTRA_CREDIT);
                              }
                              else
                              {
                                    Embed.error(event, "** %s ** is not a valid entry", content);
                                    return;
                              }
                              Embed.success(event, "** %s ** has been set as your assignment type", assignment.getAssignmentType().getAssignmentType());
                              channel.sendMessage("""
                                      I will need your due date..
                                      Please use the following format: `M/dd/yyy`
                                      An Example: `2/9/2004`
                                      """).queue();
                              state = 8;
                              return;
                        }

                        case 8 -> {
                              if (!Checks.isValidAssignmentDate(content, classroom))
                              {
                                    Embed.error(event, "** %s ** is not a valid date.. Please try again", content);
                                    return;
                              }
                              assignment.setDueDate(LocalDate.parse(content, DateTimeFormatter.ofPattern("M/d/yyyy")));
                              Embed.success(event, "** %s ** successfully set as this assignments due date", assignment.getDueDate().toString());
                              channel.sendMessage("""
                                      Lastly I will need the time in which your assignment is due
                                      Please use the following format: `HH:mm AM/PM`
                                      An Example: `12:30pm`
                                      """).queue();
                              state = 9;
                        }

                        case 9 -> {
                              if (!Checks.checkValidTime(content))
                              {
                                    Embed.error(event, "** %s ** is not a valid time... try again!", content);
                                    return;
                              }

                              String[] time = content.split(":");

                              if (content.toLowerCase().contains("am"))
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("am", ""));

                                    assignment.setOffsetTime(OffsetTime.of(LocalTime.of(hour, minute), ZoneOffset.MIN));
                              }
                              else
                              {
                                    int hour = Integer.parseInt(time[0]);
                                    int minute = Integer.parseInt(time[1].replaceAll("pm", ""));

                                    assignment.setOffsetTime(OffsetTime.of(LocalTime.of(12 + hour, minute), ZoneOffset.MIN));
                              }

                              if (classroom.addAssignment(schoolbot, assignment))
                              {
                                    Embed.success(event, "** %s ** has successfully been added to ** %s **", assignment.getName(), classroom.getClassName());
                              }
                              else
                              {
                                    Embed.error(event, "Could not add assignment.. try again!");
                                    jda.removeEventListener(this);
                                    return;
                              }
                        }


                  }

            }
      }
}


