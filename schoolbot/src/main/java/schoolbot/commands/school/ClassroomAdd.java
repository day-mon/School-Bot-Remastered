package schoolbot.commands.school;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Embed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class ClassroomAdd extends Command
{
      public ClassroomAdd(Command parent)
      {
            super(parent, "", "", 0);
            addSelfPermissions(Permission.ADMINISTRATOR);
            addFlags(CommandFlag.DATABASE);
      }

      @Override
      public void run(CommandEvent event)
      {
            if (event.getGuildSchools().size() == 0)
            {
                  Embed.error(event, "This server has no schools");
                  return;
            }

            event.sendMessage("Do you attend a University of Pittsburgh Campus ? ");
            event.getJDA().addEventListener(new ClassAddStateMachine(event));
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
                  JDA jda = event.getJDA();

                  if (message.equalsIgnoreCase("stop"))
                  {
                        channel.sendMessage("Okay aborting..").queue();
                        jda.removeEventListener(this);
                        return;
                  }

                  // TODO: Put in support for other schools other than pitt classes, also update database so the fields that are in union wth pitt class are NotNull


                  switch (state)
                  {
                        case 1 -> {
                              schoolClass = new Classroom();
                              schoolClass.setGuildID(guild.getIdLong());
                              if (message.equalsIgnoreCase("Yes") || message.equalsIgnoreCase("y"))
                              {
                                    boolean isDown = false;
                                    try
                                    {
                                          isDown = Jsoup.connect("https://psmobile.pitt.edu/app/catalog/classSearch").get().text().contains("PeopleSoft Monthly Maintenance in Progress");

                                    }
                                    catch (Exception e)
                                    {
                                          isDown = true;
                                          e.printStackTrace();
                                    }

                                    if (isDown)
                                    {
                                          Embed.error(event, "People soft is currently down for maintenance **OR** I could not connect to PeopleSoft");
                                          jda.removeEventListener(this);
                                          return;
                                    }
                              }
                              channel.sendMessage("What campus do you attend. (Main, Johnstown, Titusville, or Bradford): ").queue();
                              state = 2;
                        }
                        case 2 -> {
                              for (School schoolInDb : cachedSchools)
                              {
                                    String potSchoolName = schoolInDb.getSchoolName();
                                    if (potSchoolName.contains("University of Pittsburgh"))
                                    {
                                          if (message.equalsIgnoreCase("main"))
                                          {
                                                if (potSchoolName.equalsIgnoreCase("University of Pittsburgh"))
                                                {
                                                      channel.sendMessageFormat("Coolio.. This server has %s as a school. We can now continue.", potSchoolName).queue();
                                                      channel.sendMessageFormat("Pog sauce %s I will now need your term\n Here are some valid entries: `Fall 2020, Summer 2019, Spring 2021`", Emoji.SMILEY_FACE.getAsChat()).queue();
                                                      schoolClass.setSchoolID(schoolInDb.getSchoolID());
                                                      schoolClass.setSchool(schoolInDb);
                                                      state = 3;
                                                      return;
                                                }
                                          }
                                          else if (potSchoolName.toLowerCase().contains(message.toLowerCase()))
                                          {
                                                channel.sendMessageFormat("Coolio.. This server has %s as a school. We can now continue.", potSchoolName).queue();
                                                channel.sendMessageFormat("Pog sauce %s I will now need your term\n Here are some valid entries: `Fall 2020, Summer 2019, Spring 2021`", Emoji.SMILEY_FACE.getAsChat()).queue();
                                                schoolClass.setSchoolID(schoolInDb.getSchoolID());
                                                schoolClass.setSchool(schoolInDb);

                                                state = 3;
                                                return;
                                          }
                                    }
                              }
                              Embed.error(event, "School could not be found. Please use the [school add] command to add the school to this server");
                              state = 1;
                              jda.removeEventListener(this);
                              return;
                        }
                        case 3 -> {
                              int term = termValidator(message);
                              if (term == -1)
                              {
                                    Embed.error(event, "Not a valid term. Aborting..\n" +
                                            "Reason for Aborting\n" +
                                            "1. **Term is either to old or too far ahead in the future**\n" +
                                            "2. **You mistyped the term**\n" +
                                            "3. **You did not input a valid season**");
                                    jda.removeEventListener(this);
                                    state = 1;
                                    break;
                              }
                              schoolClass.setTerm(termFixed(message));
                              CLASS_SEARCH_URL += term + "/";
                              channel.sendMessage("What is your class #\n Hint: This can normally be found on your syllabus, psmobile or peoplesoft, or in the link of your class ").queue();
                              state = 4;
                              return;
                        }
                        case 4 -> {
                              CLASS_SEARCH_URL += message;

                              School school = schoolClass.getSchool();
                              schoolClass.setURL(CLASS_SEARCH_URL);

                              commandEvent.addPittClass(commandEvent, schoolClass);


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
            if (!yearString.chars().anyMatch(Character::isDigit)) return -1;

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

            int term = (((userMillennium * 100) + userTrailingYear) * 10) + map.get(season);
            return term;
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