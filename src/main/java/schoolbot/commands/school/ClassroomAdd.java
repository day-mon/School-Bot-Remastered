package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.StateMachine;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;
import schoolbot.util.Processor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class ClassroomAdd extends Command
{

      public ClassroomAdd(Command parent)
      {
            super(parent, "Adds a class given a target school", "[none]", 0);
            addSelfPermissions(Permission.MANAGE_ROLES);
            addFlags(CommandFlag.DATABASE);
      }


      /**
       * Returns true if PeopleSoft is up and functioning if not it return false
       * The values parameter is all of the state machines possible values
       *
       * @param values All of the state machines possible values
       * @return False is PeopleSoft is up and functioning. Otherwise fails
       */
      private static boolean isDown(@NotNull StateMachineValues values)
      {
            var jda = values.getJda();
            var event = values.getCommandEvent();
            var machine = values.getMachine();

            Connection.Response response;
            try
            {
                  response = Jsoup.connect("https://psmobile.pitt.edu/app/catalog/classSearch")
                          .followRedirects(true)
                          .execute();

            }
            catch (Exception e)
            {
                  Embed.error(event, "Error while attempting to connect to PeopleSoft");
                  jda.removeEventListener(machine);
                  return true;
            }


            if (response.url().toString().equals("https://prd.ps.pitt.edu/Maintenance.html"))
            {
                  Embed.error(event, "People soft is currently down for maintenance");
                  jda.removeEventListener(machine);
                  return true;
            }
            return false;
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

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var jda = event.getJDA();
            var schools = event.getGuildSchools();


            if (schools.isEmpty())
            {
                  Embed.error(event, "This server has no schools");
                  return;
            }

            Embed.information(event, """
                    I use a Special System for any schools that belong to the **University of Pittsburgh**
                                        
                    This is Limited to the: Main Campus, Johnstown Campus, Bradford Campus, and the Titusville Campus
                                        
                    So with that being said, to begin. Do you attend any of the University of Pittsburgh Campuses?
                    """);
            jda.addEventListener(new ClassAddStateMachine(event));


      }

      public static class ClassAddStateMachine extends ListenerAdapter implements StateMachine
      {
            private int state = 1;
            private String CLASS_SEARCH_URL = "https://psmobile.pitt.edu/app/catalog/classsection/UPITT/";
            private final StateMachineValues values;

            public ClassAddStateMachine(CommandEvent event)
            {
                  values = new StateMachineValues(event, this);
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {

                  long authorId = values.getAuthorId();
                  long channelId = values.getChannelId();

                  var requirementsMet = Checks.eventMeetsPrerequisites(values);

                  if (!requirementsMet)
                  {
                        return;
                  }


                  String message = event.getMessage().getContentRaw();
                  var jda = values.getJda();
                  var channel = event.getChannel();
                  var commandEvent = values.getCommandEvent();
                  var guild = event.getGuild();

                  // TODO: Put in support for other schools other than pitt classes


                  switch (state)
                  {
                        case 1 -> {
                              values.getClassroom().setGuildID(guild.getIdLong());

                              if (message.equalsIgnoreCase("Yes") || message.equalsIgnoreCase("y"))
                              {
                                    if (isDown(values))
                                    {
                                          return;
                                    }

                                    var success = Processor.processGenericList(values, values.getPittClass(), School.class);

                                    if (success == 1)
                                    {
                                          var school = values.getSchool();
                                          channel.sendMessageFormat("""
                                                                                                    
                                                  ** %s ** has been selected successfully. I will now need your term. I only understand pitt term like
                                                  ```
                                                  Fall 2021
                                                  Spring 2020
                                                  Summer 2019
                                                                                
                                                  Format: <Season> <Year number>
                                                  ```
                                                  """, school.getName()).queue();
                                          values.getClassroom().setSchool(school);
                                          state = 3;
                                    }
                                    else if (success == 2) state = 2;
                                    // else case here dont forget


                              }
                              else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("nah"))
                              {
                                    channel.sendMessage("We will start by getting the school you want to add the class to").queue();
                                    var schools = Processor.processGenericList(values, values.getSchoolList(), School.class);

                                    if (schools == 1)
                                    {
                                          commandEvent.sendMessage("This school has been selected because there is only one available");
                                    }
                                    else if (schools > 1)
                                    {
                                          commandEvent.sendMessage("Please select a school from the page numbers");
                                          state = 10;
                                    }
                              }
                              else
                              {
                                    channel.sendMessageFormat("** %s ** is not a yes or no answer.. Try again", message).queue();
                              }


                        }
                        case 2 -> {

                              var pittClasses = values.getPittClass();
                              var success = Processor.validateMessage(event, pittClasses);

                              if (success == null)
                              {
                                    return;
                              }

                              values.getClassroom().setSchool(success);


                              Embed.success(event, "Successfully set school to %s", success.getName());
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
                              values.getClassroom().setTerm(termFixed(message));
                              CLASS_SEARCH_URL += term + "/";
                              channel.sendMessage("""
                                      What is your class number.
                                                                            
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


                              values.getClassroom().setURL(CLASS_SEARCH_URL);
                              values.getClassroom().setNumber(Integer.parseInt(message));

                              final var classroom = values.getClassroom();
                              final var school = classroom.getSchool();


                              commandEvent.getCommandThreadPool().execute(() -> commandEvent.addPittClass(classroom));

                              jda.removeEventListener(this);
                        }


                        case 10 -> {
                              var valid = Processor.validateMessage(values, values.getSchoolList());

                              if (!valid)
                              {
                                    return;
                              }
                        }

                  }

                  // Custom school states

            }

      }
}