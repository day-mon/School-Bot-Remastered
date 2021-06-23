
package schoolbot.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import schoolbot.Constants;
import schoolbot.Schoolbot;
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


public class School implements Paginatable
{
      private String name;
      private String URL;
      private String emailSuffix;
      private long guildID;
      private long roleID;
      private int id;
      private boolean isPittSchool;
      private List<Classroom> classroomList;
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

      private static int evaluateConstraints(Guild guild, Classroom classroom, Document document)
      {

            if (guild.getRoles().size() == Constants.MAX_GUILD_ROLE_COUNT)
            {
                  return 1;
            }

            if (guild.getTextChannels().size() == Constants.MAX_GUILD_TEXTCHANNEL_COUNT)
            {
                  return 2;
            }

            if (document.text().contains("Unexpected error occurred."))
            {
                  return 3;
            }


            // The class name will always be the primary head so we can just automatically it will be a list(0)
            String className = document.getElementsByClass("primary-head").get(0).text();
            classroom.setName(className);

            if (className.length() >= 100)
            {
                  return 4;
            }


            boolean duplicateClass = classroom.getSchool().getClassroomList()
                    .stream()
                    .anyMatch(classrooms ->
                            className.equalsIgnoreCase(classrooms.getName())
                            &&
                            classroom.getTerm().equalsIgnoreCase(classrooms.getTerm())
                            &&
                            classroom.getNumber() == classrooms.getNumber());

            if (duplicateClass)
            {
                  return 5;
            }

            return -1;

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

      public MessageEmbed getAsEmbed(@NotNull Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.roleID);

            return getAsEmbedBuilder(schoolbot)
                    .build();
      }

      private static void initDate(Classroom classroom, String date)
      {
            String[] dates = date.split("-");
            String[] start = dates[0].trim().split("/");
            String[] end = dates[1].trim().split("/");

            int sYear = Integer.parseInt(start[2]);
            int sMonth = Integer.parseInt(start[0]);
            int sDay = Integer.parseInt(start[1]);


            int eYear = Integer.parseInt(end[2]);
            int eMonth = Integer.parseInt(end[0]);
            int eDay = Integer.parseInt(end[1]);

            classroom.setStartDate(Date.valueOf(LocalDate.of(sYear, sMonth, sDay)));
            classroom.setEndDate(Date.valueOf(LocalDate.of(eYear, eMonth, eDay)));
      }


      public void setURL(String URL)
      {
            this.URL = URL;
      }

      private static void evaluateInstructor(CommandEvent event, Classroom classroom, String text)
      {
            String name = classroom.getSchool().getName();

            event.getSchoolsProfessors(name)
                    .stream()
                    .filter(professor ->
                    {
                          if (professor.getFirstName().equalsIgnoreCase("Staff") && text.toLowerCase().contains("staff"))
                          {
                                return true;
                          }

                          String firstAndLast = professor.getFirstName() + " " + professor.getLastName();

                          return firstAndLast.equalsIgnoreCase(text);
                    })
                    .findFirst().map(professor ->
            {
                  classroom.setInstructor(text);
                  classroom.setProfessor(professor);
                  return professor;

            }).orElseGet(() ->
            {
                  event.getChannel().sendMessage("This professor has not been found in my database for this server... adding him now!").queue();
                  int length = text.split("\\s+").length;

                  String firstName = text.split("\\s+")[0];
                  String lastName = (length < 2) ? "Unknown" : text.split("\\s+")[1];


                  Professor prof = new Professor(
                          firstName,
                          lastName,
                          classroom.getSchool()
                  );

                  if (!event.addProfessor(prof))
                  {
                        removeSequence(event, classroom);
                        return null;
                  }
                  classroom.setProfessor(prof);
                  return prof;
            });
      }

      private static boolean evaluateCampus(Classroom classroom, String campus)
      {
            int length = classroom.getSchool().getName().split("\\s").length;
            String classCampus = classroom.getSchool().getName().split("\\s+")[length - 1];
            // This may seem confusing me a class can have more than one campus so I am just going to grab the first 1
            String classesCampus = classCampus.split("\\s+")[0];

            if (!campus.toLowerCase().contains(classesCampus.toLowerCase()))
            {
                  return false;
            }

            if (classroom.getLocation() == null)
            {
                  classroom.setLocation(classCampus);
            }
            return true;

      }

      private static void removeSequence(CommandEvent event, Classroom classroom)
      {
            var guild = event.getGuild();

            guild.getRoleById(classroom.getRoleID()).delete().queue();

            event.removeProfessor(classroom.getProfessor());
            event.getGuild().getRoleById(classroom.getRoleID()).delete().queue();
            event.getGuild().getTextChannelById(classroom.getChannelID()).delete().queue();
      }

      public void setClassroomList(List<Classroom> classroomList)
      {
            this.classroomList = classroomList;
      }

      public EmbedBuilder getAsEmbedBuilder(@NotNull Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.roleID);

            if (URL.isBlank() || URL.isBlank() || URL == null)
            {
                  this.URL = "https://schoolbot.dev";
            }

            return new EmbedBuilder()
                    .setTitle(Emoji.BOOKS.getAsChat() + " " + this.name + " " + Emoji.BOOKS.getAsChat(), URL)
                    .addField("Role", role == null ? "N/A" : role.getAsMention(), false)
                    .addField("Email Suffix", this.emailSuffix, false)
                    .addField("Amount of Classes", String.valueOf(this.classroomList.size()), false)
                    .addField("Amount of Professors", String.valueOf(this.professorList.size()), false)
                    .addField("School ID", String.valueOf(id), false)
                    .setColor(role == null ? Constants.DEFAULT_EMBED_COLOR : role.getColor())
                    .setTimestamp(Instant.now());
      }

      public void addPittClass(CommandEvent event, Classroom schoolClass)
      {

            String save = "";
            Document document;

            var guild = event.getGuild();
            var channel = event.getChannel();
            var schoolbot = event.getSchoolbot();


            try
            {
                  document = Jsoup.connect(schoolClass.getURL()).get();
            }
            catch (Exception e)
            {
                  Embed.error(event, "Could not connect to Peoplesoft.. Try again later!");
                  e.printStackTrace();
                  return;
            }


            var evaluateErrors = evaluateConstraints(guild, schoolClass, document);

            switch (evaluateErrors)
            {
                  case 1 -> {
                        Embed.error(event, "Cannot create a role because your server is already at capacity");
                        return;
                  }
                  case 2 -> {
                        Embed.error(event, "Cannot create text channel because you are at capacity");
                        return;
                  }
                  case 3 -> {
                        Embed.error(event, "Class information not found.");
                        return;
                  }
                  case 4 -> {
                        Embed.error(event, "Class name has a name longer than 100 characters.. Please add the class manually");
                        return;
                  }
                  case 5 -> {
                        Embed.error(event, "This class already exist for ** %s **", schoolClass.getSchoolWithoutID().getName());
                        return;
                  }

            }


            String className = schoolClass.getName();


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

            var role = guild.createRole()
                    .setName(className.toLowerCase().replaceAll("\\s", "-"))
                    .setColor(new Random().nextInt(0xFFFFFF))
                    .complete();

            // Makes channel only viewable by the role that was just created
            var textChannel = guild.createTextChannel(className)
                    .addPermissionOverride(role, Permission.ALL_CHANNEL_PERMISSIONS, 0L)
                    .addPermissionOverride(guild.getPublicRole(), 0L, Permission.ALL_CHANNEL_PERMISSIONS)
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
                        case "Career" -> schoolClass.setLevel(textRight);
                        case "Dates" -> initDate(schoolClass, textRight);
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
                        case "Instructor(s)" -> evaluateInstructor(event, schoolClass, textRight);
                        case "Meets" -> {
                              schoolClass.setTime(textRight);
                              save = textRight;
                        }
                        case "Room" -> schoolClass.setRoom(textRight);
                        case "Location" -> schoolClass.setLocation(textRight);
                        case "Campus" -> {
                              var success = evaluateCampus(schoolClass, textRight);
                              if (!success)
                              {
                                    Embed.error(event, "");
                                    removeSequence(event, schoolClass);
                                    return;
                              }
                              right = elementsRight.size();
                        }
                  }
            }


            schoolClass.setWasAutoFilled(true);

            int classCheck = DatabaseUtil.addClassPitt(event, schoolClass);
            if (classCheck == -1)
            {
                  Embed.error(event, """
                          Database failed to add ** %s **
                          """, schoolClass.getName());
                  removeSequence(event, schoolClass);
                  return;
            }
            schoolClass.setId(classCheck);
            Parser.classTime(schoolbot, save, schoolClass);
            this.classroomList.add(schoolClass);
            this.professorList.add(schoolClass.getProfessor());
            channel.sendMessageEmbeds(schoolClass.getAsEmbed(schoolbot)).queue();


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


      /**
       * Returns professor if found null if not
       * The id parameter is the professors id in correlation to the database
       *
       * @param id A potential professor id
       * @return Professor if found <b>NULL</b> if not found
       */
      @Nullable
      public Professor getProfessorByID(int id)
      {
            return professorList
                    .stream()
                    .filter(professor -> professor.getId() == id)
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

      @Override
      public String toString()
      {
            return "School {" +
                   "name='" + name + '\'' +
                   ", URL='" + URL + '\'' +
                   ", emailSuffix='" + emailSuffix + '\'' +
                   ", guildID=" + guildID +
                   ", roleID=" + roleID +
                   ", id=" + id +
                   ", isPittSchool=" + isPittSchool +
                   ", classroomList=" + classroomList +
                   ", professorList=" + professorList +
                   '}';
      }
}

