package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.school.Classroom;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ClassroomAdd extends Command
{

      public ClassroomAdd(Command parent)
      {
            super(parent, "Adds a class given a target school", "[none]", 0);
            addUsageExample("N/A");
            addSelfPermissions(Permission.MANAGE_ROLES);
            addFlags(CommandFlag.DATABASE);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            if (args.isEmpty())
            {
                  if (event.getGuildSchools().isEmpty())
                  {
                        Embed.error(event, "This server has no schools");
                        return;
                  }

                  event.sendMessage("Do you attend a University of Pittsburgh Campus ? ");
                  event.getJDA().addEventListener(new ClassAddStateMachine(event));
            }

      }

      public static class ClassAddStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private int state = 1;
            private final CommandEvent commandEvent;
            private String CLASS_SEARCH_URL = "https://psmobile.pitt.edu/app/catalog/classsection/UPITT/";
            private Classroom schoolClass;

            public ClassAddStateMachine(CommandEvent event)
            {
                  this.channelID = event.getChannel().getIdLong();
                  this.authorID = event.getUser().getIdLong();
                  this.commandEvent = event;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().isBot()) return;
                  if (event.getAuthor().getIdLong() != authorID) return;
                  if (event.getChannel().getIdLong() != channelID) return;


                  Guild guild = event.getGuild();
                  MessageChannel channel = event.getChannel();
                  String message = event.getMessage().getContentRaw();
                  List<School> cachedSchools = commandEvent.getGuildSchools();
                  List<School> pittSchools = commandEvent.getGuildSchools()
                          .stream()
                          .filter(School::isPittSchool)
                          .collect(Collectors.toList());
                  JDA jda = event.getJDA();

                  if (message.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Okay aborting..").queue();
                        jda.removeEventListener(this);
                        return;
                  }

                  // TODO: Put in support for other schools other than pitt classes


                  switch (state)
                  {
                        case 1 -> {
                              schoolClass = new Classroom();
                              schoolClass.setGuildID(guild.getIdLong());

                              if (message.equalsIgnoreCase("Yes") || message.equalsIgnoreCase("y"))
                              {
                                    Connection.Response response = null;
                                    try
                                    {
                                          response = Jsoup.connect("https://psmobile.pitt.edu/app/catalog/classSearch")
                                                  .followRedirects(true)
                                                  .execute();

                                    }
                                    catch (Exception e)
                                    {
                                          e.printStackTrace();
                                          return;
                                    }


                                    if (response.url().toString().equals("https://prd.ps.pitt.edu/Maintenance.html"))
                                    {
                                          Embed.error(event, "People soft is currently down for maintenance");
                                          jda.removeEventListener(this);
                                          return;
                                    }

                                    if (pittSchools.isEmpty())
                                    {
                                          Embed.error(event, "{} has no pitt schools", event.getGuild().getName());
                                    }
                                    else if (pittSchools.size() == 1)
                                    {
                                          School school = pittSchools.get(0);
                                          this.schoolClass.setSchool(school);
                                          event.getChannel().sendMessage(school.getAsEmbed(commandEvent.getSchoolbot())).queue();
                                          event.getChannel().sendMessage(school.getName() + " has automatically been selected because it is the only school available").queue();
                                          channel.sendMessage("""
                                                  I will now need your term. I only understand pitt term like
                                                  ```
                                                  Fall 2021
                                                  Spring 2020
                                                  Summer 2019
                                                                                
                                                  Format: <Season> <Year number>
                                                  ```
                                                  """).queue();
                                          state = 3;
                                    }
                                    else
                                    {
                                          commandEvent.sendAsPaginatorWithPageNumbers(pittSchools);
                                          commandEvent.sendMessage("Please pick the campus based off the page numbers :)");
                                          state = 2;
                                    }
                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("nah"))
                              {
                                    // other school logic
                              }
                              else
                              {
                                    channel.sendMessageFormat("** %s ** is not a yes or no answer.. Try again", message).queue();
                              }


                        }
                        case 2 -> {
                              if (!Checks.isNumber(message))
                              {
                                    Embed.error(commandEvent, "%s is not a number", message);
                                    return;
                              }


                              int number = Integer.parseInt(message);

                              if (!Checks.between(number, pittSchools.size()))
                              {
                                    Embed.error(event, "%d is not in between 1 and %d", number, pittSchools.size());
                                    return;
                              }
                              schoolClass.setSchool(pittSchools.get(number - 1));
                              Embed.success(event, "Successfully set school to %s", schoolClass.getSchool().getName());
                              channel.sendMessage("""
                                      I will now need your term. I only understand pitt term like
                                      ```
                                      Fall 2021
                                      Spring 2020
                                      Summer 2019
                                                                    
                                      Format: <Season> <Year number>
                                      ```
                                      """).queue();
                              state = 3;
                        }
                        case 3 -> {
                              int term = termValidator(message);
                              if (term == -1)
                              {
                                    Embed.error(event, """
                                            Not a valid term. Aborting..
                                            Reason for Aborting
                                            1. **Term is either to old or too far ahead in the future**
                                            2. **You mistyped the term**
                                            3. **You did not input a valid season**""");
                                    jda.removeEventListener(this);
                                    state = 1;
                                    break;
                              }
                              schoolClass.setTerm(termFixed(message));
                              CLASS_SEARCH_URL += term + "/";
                              channel.sendMessage("""
                                      What is your class number
                                      `Hint: This can normally be found on your Syllabus, PsMobile or PeopleSoft, or in the link of your class`
                                      """).queue();
                              state = 4;
                        }
                        case 4 -> {

                              if (!Checks.isNumber(message))
                              {
                                    Embed.notANumberError(event, message);
                                    return;
                              }
                              CLASS_SEARCH_URL += message;

                              School school = schoolClass.getSchool();
                              schoolClass.setURL(CLASS_SEARCH_URL);
                              schoolClass.setNumber(Integer.parseInt(message));
                              commandEvent.getCommandThreadPool().execute(() -> school.addPittClass(commandEvent, schoolClass));
                              event.getJDA().removeEventListener(this);
                        }
                  }
            }
      }


      private static int termValidator(String content)
      {
            Map<String, Integer> map = Map.of("fall", 1, "spring", 4, "summer", 7);

            if (content.split("\\s+").length != 2) return -1;

            String season = content.split("\\s")[0].toLowerCase();
            String yearString = content.split("\\s")[1];

            if (yearString.length() != 4) return -1;
            if (!season.chars().allMatch(Character::isLetter)) return -1;
            if (yearString.chars().noneMatch(Character::isDigit)) return -1;

            // Computer Generated
            int curYear = LocalDateTime.now().getYear();
            int millennium = curYear / 1000;
            int trailingYear = curYear % 100;

            //User
            int yearInt = Integer.parseInt(yearString);
            int userMillennium = yearInt / 1000;
            int userTrailingYear = (season.equalsIgnoreCase("fall")) ? (yearInt % 100) + 1 : (yearInt % 100);
            if (!season.equalsIgnoreCase("spring") && !season.equalsIgnoreCase("summer") && !season.equalsIgnoreCase("fall"))
                  return -1;
            if (millennium != userMillennium) return -1;
            if (userTrailingYear == trailingYear || userTrailingYear == trailingYear + 1) ;
            else return -1;

            return (((userMillennium * 100) + userTrailingYear) * 10) + map.get(season);
      }

      private static String termFixed(String term)
      {
            char[] termCharArr = term.split("\\s")[0].toCharArray();

            for (int i = 0; i < termCharArr.length; i++)
            {
                  if (i == 0)
                  {
                        termCharArr[i] = Character.toUpperCase(termCharArr[i]);
                  }
                  else
                  {
                        termCharArr[i] = Character.toLowerCase(termCharArr[i]);
                  }
            }

            return String.valueOf(termCharArr) + " " + term.split("\\s+")[1];
      }
}