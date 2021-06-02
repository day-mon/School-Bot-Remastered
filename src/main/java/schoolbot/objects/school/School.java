
package schoolbot.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;
import schoolbot.objects.misc.Paginatable;
import schoolbot.util.DatabaseUtil;
import schoolbot.util.Embed;
import schoolbot.util.Parser;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class School implements Paginatable
{
      private String name;
      private String URL;
      private String emailSuffix;
      private long guildID;
      private long roleID;
      private int id;
      private boolean isPittSchool;
      private final List<Classroom> classroomList;
      private final List<Professor> professorList;


      public School()
      {
            this.roleID = 0L;
            this.name = "N/A";
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();
      }

      public School(String name, String suffix, long roleID, long guildID, String url)
      {
            this.name = name;
            this.emailSuffix = suffix;
            this.roleID = roleID;
            this.guildID = guildID;
            this.URL = url;
            this.isPittSchool = name.contains("University of Pittsburgh");
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();

      }


      public School(String schoolName)
      {
            this.name = schoolName;
            this.roleID = 0L;
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();
      }

      public School(long guildID, String schoolName, String emailSuffix)
      {
            this.name = schoolName;
            this.guildID = guildID;
            this.emailSuffix = emailSuffix;
            this.roleID = 0L;
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();
      }


      public School(int id, String name, long roleID, boolean isPittSchool, String email_suffix, String url)
      {
            this.id = id;
            this.name = name;
            this.roleID = roleID;
            this.isPittSchool = isPittSchool;
            this.emailSuffix = email_suffix;
            this.URL = url;
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();
      }

      public School(int id, String name, long roleID, boolean isPittSchool, long guildID, String emailSuffix, String url)
      {
            this.id = id;
            this.name = name;
            this.roleID = roleID;
            this.isPittSchool = isPittSchool;
            this.guildID = guildID;
            this.emailSuffix = emailSuffix;
            this.URL = url;
            this.professorList = new ArrayList<>();
            this.classroomList = new ArrayList<>();
      }

      /**
       * @return school name
       */
      public String getName()
      {
            return name;
      }

      public void addClass(Classroom classroom)
      {
            classroomList.add(classroom);
      }

      public void addProfessor(Professor professor)
      {
            professorList.add(professor);
            professor.setProfessorsSchool(this);
      }

      public void removeProfessor(Professor professor)
      {
            professorList.remove(professor);
            professor.setProfessorsSchool(null);
      }

      public void removeClass(Classroom classroom)
      {
            classroomList.remove(classroom);
            classroom.getProfessor().removeClass(classroom);
      }


      public String getEmailSuffix()
      {
            return emailSuffix;
      }


      public long getGuildID()
      {
            return guildID;
      }



      public void setGuildID(long guildID)
      {
            this.guildID = guildID;
      }


      public List<Professor> getProfessorList()
      {
            return professorList;
      }


      public List<Classroom> getClassroomList()
      {
            return classroomList;
      }

      public int getClassesSize()
      {
            return classroomList.size();
      }

      public String getURL()
      {
            return URL;
      }

      /**
       * @param name
       */
      public void setName(String name)
      {
            this.name = name;
      }

      public void setEmailSuffix(String emailSuffix)
      {
            this.emailSuffix = emailSuffix;
      }

      public boolean isPittSchool()
      {
            return isPittSchool;
      }

      public void setIsPittSchool(boolean isPittSchool)
      {
            this.isPittSchool = isPittSchool;
      }

      public MessageEmbed getAsEmbed(Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.roleID);

            return getAsEmbedBuilder(schoolbot)
                    .build();
      }

      public EmbedBuilder getAsEmbedBuilder(Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.roleID);

            return new EmbedBuilder()
                    .setTitle(Emoji.BOOKS.getAsChat() + " " + this.name + " " + Emoji.BOOKS.getAsChat(), URL)
                    .addField("Role", role == null ? "N/A" : role.getAsMention(), false)
                    .addField("Email Suffix", this.emailSuffix, false)
                    .addField("Amount of Classes", String.valueOf(this.classroomList.size()), false)
                    .addField("Amount of Professors", String.valueOf(this.professorList.size()), false)
                    .addField("School ID", String.valueOf(id), false)
                    .setColor(role == null ? SchoolbotConstants.DEFAULT_EMBED_COLOR : role.getColor())
                    .setTimestamp(Instant.now());
      }


      public void setURL(String URL)
      {
            this.URL = URL;
      }

      public void addPittClass(CommandEvent event, Classroom schoolClass)
      {

            ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(runnable -> new Thread("StateMachine-Thread"));

            executorService.execute(() ->
            {

                  String save = "";
                  Logger LOGGER = event.getSchoolbot().getLogger();
                  MessageChannel channel = event.getChannel();
                  Document document = null;
                  Guild guild = event.getGuild();

                  try
                  {
                        document = Jsoup.connect(schoolClass.getURL()).get();
                  }
                  catch (Exception e)
                  {
                        Embed.error(event, "Could not connect to Peoplesoft.. Try again later!", e);
                        e.printStackTrace();
                        return;
                  }

                  if (document.text().contains("Unexpected error occurred."))
                  {
                        Embed.error(event, "Class information not found.");
                        return;
                  }

                  LOGGER.info("THREAD CHECK");
                  LOGGER.info("THREAD CHECK");
                  LOGGER.info("THREAD CHECK");
                  LOGGER.info("THREAD CHECK");
                  LOGGER.info("THREAD CHECK");
                  LOGGER.info("THREAD CHECK");


                  // The class name will always be the primary head so we can just automatically it will be a list(0)
                  String className = document.getElementsByClass("primary-head").get(0).text();
                  schoolClass.setName(className);


                  // Check if class already exist just in case...
                  boolean duplicateClass = schoolClass.getSchool().getClassroomList()
                          .stream()
                          .anyMatch(classroom ->
                                  className.equalsIgnoreCase(classroom.getName())
                                  && classroom.getTerm().equalsIgnoreCase(schoolClass.getTerm()));


                  if (duplicateClass)
                  {
                        Embed.error(event, "This class already exist for ** %s **", schoolClass.getSchoolWithoutID().getName());
                        return;
                  }

                  // All Elements on left side of class page
                  Elements elementsLeft = document.getElementsByClass("pull-left");

                  // All Elements on right side of class page
                  Elements elementsRight = document.getElementsByClass("pull-right");

                  // Identifier for class Ex: CS 0015, COMP 101;
                  String[] identifier = document.getElementsByClass("page-title  with-back-btn").get(0).text().split("\\s");

                  // Parsing the subject and the class number
                  String subject = identifier[0];
                  String classNameNum = identifier[identifier.length - 1];
                  String subjectAndClassNameAndNum = subject + " " + classNameNum;


                  // Setting classname and identifier
                  schoolClass.setClassIdentifier(subjectAndClassNameAndNum);


                  // TODO: when cleaning code up make this a function

                  if (guild.getRoles().size() == SchoolbotConstants.MAX_GUILD_ROLE_COUNT)
                  {
                        channel.sendMessage("Cannot create a role because your server is already at capacity").queue();
                        return;
                  }

                  if (guild.getTextChannels().size() == SchoolbotConstants.MAX_GUILD_TEXTCHANNEL_COUNT)
                  {
                        channel.sendMessage("Cannot create text channel because you are at capacity").queue();
                        return;
                  }

                  if (className.length() >= 100)
                  {
                        channel.sendMessage("Class name has a name longer than 100 characters.. Please add the class manually").queue();
                        return;
                  }

                  Role role = guild.createRole()
                          .setName(className.toLowerCase().replaceAll("\\s", "-"))
                          .setColor(new Random().nextInt(0xFFFFFF))
                          .complete();
                  TextChannel textChannel = guild.createTextChannel(className)
                          .addRolePermissionOverride(role.getIdLong(), Permission.ALL_TEXT_PERMISSIONS, 0L)
                          .removePermissionOverride(guildID)
                          .complete();

                  schoolClass.setRoleID(role.getIdLong());
                  schoolClass.setChannelID(textChannel.getIdLong());

                  for (int left = 1, right = 2; right <= elementsRight.size() - 1; left++, right++)
                  {

                        // Gotta check if the current tag w e are on is a div because if its not we are not on something we wanna scrape.
                        while (!elementsRight.get(right).tag().getName().equalsIgnoreCase("div"))
                        {
                              right++;
                        }

                        // Text on left side of class page (i.e Description, Class Times, etc);
                        String textLeft = elementsLeft.get(left).text();
                        // Text on right side of class page (i.e actual data)
                        String textRight = elementsRight.get(right).text();


                        switch (textLeft)
                        {
                              case "Class Number" -> schoolClass.setNumber(Integer.parseInt(textRight));
                              case "Career" -> schoolClass.setLevel(textRight);
                              case "Dates" -> {
                                    String[] dates = textRight.split("-");
                                    String[] start = dates[0].trim().split("/");
                                    String[] end = dates[1].trim().split("/");

                                    int sYear = Integer.parseInt(start[2]);
                                    int sMonth = Integer.parseInt(start[0]);
                                    int sDay = Integer.parseInt(start[1]);


                                    int eYear = Integer.parseInt(end[2]);
                                    int eMonth = Integer.parseInt(end[0]);
                                    int eDay = Integer.parseInt(end[1]);

                                    schoolClass.setStartDate(Date.valueOf(LocalDate.of(sYear, sMonth, sDay)));
                                    schoolClass.setEndDate(Date.valueOf(LocalDate.of(eYear, eMonth, eDay)));
                              }
                              case "Units" -> schoolClass.setCreditAmount(Integer.parseInt(textRight.substring(0, 1)));
                              case "Description" -> {
                                    String temp = textRight;
                                    if (temp.length() > 1024)
                                    {
                                          temp = temp.split("\\.")[0] + "....";
                                    }
                                    schoolClass.setDescription(temp);
                              }
                              case "Enrollment Requirements" -> schoolClass.setPrerequisite(textRight);
                              case "Instructor(s)" -> {
                                    List<Professor> professorList = event.getSchoolsProfessors(name);
                                    boolean found = false;
                                    Professor professorFound = null;
                                    if (!professorList.isEmpty())
                                    {
                                          for (Professor professor : professorList)
                                          {
                                                String firstAndLast = professor.getFirstName() + " " + professor.getLastName();
                                                if (firstAndLast.equalsIgnoreCase(textRight))
                                                {
                                                      // Could change the found flag to just if professorFound == null
                                                      // Clean this up later
                                                      found = true;
                                                      schoolClass.setInstructor(textRight);
                                                      schoolClass.setProfessor(professor);
                                                      break;
                                                }
                                          }
                                    }


                                    if (!found)
                                    {
                                          channel.sendMessage("This professor has not been found in my database for this server... adding him now!").queue();
                                          int length = textRight.split("\\s+").length;

                                          String firstName = textRight.split("\\s+")[0];
                                          String lastName = (length < 2) ? "Unknown" : textRight.split("\\s+")[1];


                                          Professor prof = new Professor(
                                                  firstName,
                                                  lastName,
                                                  schoolClass.getSchool()
                                          );

                                          if (!event.addProfessor(event, prof))
                                          {
                                                Embed.error(event, "Could not add professor. Please try again");
                                                event.getGuild().getRoleById(schoolClass.getRoleID()).delete().queue();
                                                event.getGuild().getTextChannelById(schoolClass.getChannelID()).delete().queue();
                                                return;
                                          }
                                          schoolClass.setProfessor(prof);

                                    }
                              }

                              case "Meets" -> {
                                    schoolClass.setTime(textRight);
                                    save = textRight;
                              }
                              case "Room" -> schoolClass.setRoom(textRight);
                              case "Location" -> schoolClass.setLocation(textRight);
                              case "Campus" -> {
                                    int length = schoolClass.getSchool().getName().split("\\s").length;
                                    String campus = schoolClass.getSchool().getName().split("\\s+")[length - 1];
                                    String classesCampus = textRight.split("\\s+")[0];
                                    if (!campus.toLowerCase().contains(classesCampus.toLowerCase()))
                                    {
                                          event.removeProfessor(event, schoolClass.getProfessor());
                                          event.getGuild().getRoleById(schoolClass.getRoleID()).delete().queue();
                                          event.getGuild().getTextChannelById(schoolClass.getChannelID()).delete().queue();

                                          Embed.error(event, """
                                                  You said you goto the ** %s ** campus this class takes place on the ** %s ** campus\040
                                                  Professor %s has been removed\040
                                                  """, campus, classesCampus, schoolClass.getProfessor().getFullName());
                                          return;
                                    }
                                    if (schoolClass.getLocation() == null)
                                    {
                                          schoolClass.setLocation(textRight);
                                    }
                                    right = elementsRight.size();

                              }
                        }
                  }


                  int classCheck = DatabaseUtil.addClassPitt(event, schoolClass);
                  if (classCheck == -1)
                  {
                        Embed.error(event, """
                                Database failed to add ** %s **
                                """, schoolClass.getName());
                        event.removeProfessor(event, schoolClass.getProfessor());
                        event.getGuild().getRoleById(schoolClass.getRoleID()).delete().queue();
                        event.getGuild().getTextChannelById(schoolClass.getChannelID()).delete().queue();
                        return;
                  }
                  schoolClass.setId(classCheck);
                  Parser.classTime(event.getSchoolbot(), save, schoolClass);
                  this.classroomList.add(schoolClass);
                  this.professorList.add(schoolClass.getProfessor());
                  channel.sendMessage(schoolClass.getAsEmbed(event.getSchoolbot())).queue();
            });
      }


      public void setPittSchool(boolean pittSchool)
      {
            isPittSchool = pittSchool;
      }

      public int getID()
      {
            return id;
      }

      public void setSchoolID(int schoolID)
      {
            this.id = schoolID;
      }

      public void setRoleID(long roleID)
      {
            this.roleID = roleID;
      }

      public long getRoleID()
      {
            return roleID;
      }

      public Professor getProfessorByID(int id)
      {
            return professorList
                    .stream()
                    .filter(professor -> professor.getID() == id)
                    .findFirst()
                    .orElse(null);
      }

      public Classroom getClassroomByID(int id)
      {
            return classroomList
                    .stream()
                    .filter(classroom -> classroom.getId() == id)
                    .findFirst()
                    .orElse(null);
      }

}

