
package schoolbot.natives.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.objects.misc.Paginatable;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class School implements Paginatable
{


      private String schoolName;
      private String URL;
      private String emailSuffix;
      private long guildID;
      private long roleID;
      private int id;
      private boolean isPittSchool;
      private List<Classroom> classroomList;
      private List<Professor> professorList;


      public School()
      {
            this.roleID = 0L;
            this.schoolName = "N/A";
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();
      }

      public School(String name, String suffix, long roleID, long guildID, String url)
      {
            this.schoolName = name;
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
            this.schoolName = schoolName;
            this.roleID = 0L;
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();
      }

      public School(long guildID, String schoolName, String emailSuffix)
      {
            this.schoolName = schoolName;
            this.guildID = guildID;
            this.emailSuffix = emailSuffix;
            this.roleID = 0L;
            this.classroomList = new ArrayList<>();
            this.professorList = new ArrayList<>();
      }


      public School(int id, String name, long roleID, boolean isPittSchool, String email_suffix, String url)
      {
            this.id = id;
            this.schoolName = name;
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
            this.schoolName = name;
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
      public String getSchoolName()
      {
            return schoolName;
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
       * @param schoolName
       */
      public void setSchoolName(String schoolName)
      {
            this.schoolName = schoolName;
      }

      public void setEmailSuffix(String emailSuffix)
      {
            this.emailSuffix = emailSuffix;
      }

      public boolean getIsPittSchool()
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
                    .setTitle(Emoji.BOOKS.getAsChat() + " " + this.schoolName + " " + Emoji.BOOKS.getAsChat(), URL)
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
                  Embed.error(event, "Could not connect to Peoplesoft.. Try again later!");
                  return;
            }

            if (document.text().contains("Unexpected error occurred."))
            {
                  Embed.error(event, "Class information not found.");
                  return;
            }

            // The class name will always be the primary head so we can just automatically it will be a list(0)
            String className = document.getElementsByClass("primary-head").get(0).text();
            schoolClass.setClassName(className);


            // Check if class already exist just in case...
            boolean duplicateClass = schoolClass.getSchool().getClassroomList()
                    .stream()
                    .anyMatch(classroom ->
                            className.equalsIgnoreCase(classroom.getClassName())
                            && classroom.getTerm().equalsIgnoreCase(schoolClass.getTerm()));


            if (duplicateClass)
            {
                  Embed.error(event, "This class already exist for ** %s **", schoolClass.getSchoolWithoutID().getSchoolName());
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


            // TODO when cleaning code up make this a function

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

            guild.createRole()
                    .setName(className.toLowerCase().replaceAll("\\s", "-"))
                    .setColor(new Random().nextInt(0xFFFFFF))
                    .queue(
                            roleSuccess ->
                            {
                                  schoolClass.setRoleID(roleSuccess.getIdLong());
                                  LOGGER.info("Role successfully created");

                                  event.getGuild().createTextChannel(className)
                                          .queue(
                                                  textChannelSuccess ->
                                                  {
                                                        schoolClass.setChannelID(textChannelSuccess.getIdLong());
                                                        LOGGER.info("TextChannel successfully created");
                                                  }
                                          );
                            }
                    );


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
                        case "Class Number" -> schoolClass.setClassNumber(Integer.parseInt(textRight));
                        case "Career" -> schoolClass.setClassLevel(textRight);
                        case "Dates" -> {
                              String[] dates = textRight.split("-");
                              String[] start = dates[0].trim().split("/");
                              String[] end = dates[1].trim().split("/");
                              schoolClass.setInputClassEndDate(start);
                              schoolClass.setInputClassStartDate(end);
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
                        case "Enrollment Requirements" -> schoolClass.setPreReq(textRight);
                        case "Instructor(s)" -> {
                              List<Professor> professorList = event.getSchoolsProfessors(schoolName);
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

                        case "Meets" -> schoolClass.setClassTime(textRight);
                        case "Room" -> schoolClass.setClassRoom(textRight);
                        case "Location" -> schoolClass.setClassLocation(textRight);
                        case "Campus" -> {
                              int length = schoolClass.getSchool().getSchoolName().split("\\s").length;
                              String campus = schoolClass.getSchool().getSchoolName().split("\\s+")[length - 1];
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
                              if (schoolClass.getClassLocation() == null)
                              {
                                    schoolClass.setClassLocation(textRight);
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
                          """, schoolClass.getClassName());
                  event.removeProfessor(event, schoolClass.getProfessor());
                  event.getGuild().getRoleById(schoolClass.getRoleID()).delete().queue();
                  event.getGuild().getTextChannelById(schoolClass.getChannelID()).delete().queue();
                  return;
            }

            schoolClass.setId(classCheck);
            this.classroomList.add(schoolClass);
            this.professorList.add(schoolClass.getProfessor());
            channel.sendMessage(schoolClass.getAsEmbed(event.getSchoolbot())).queue();

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


      public enum SchoolUpdates
      {
            NAME("name"),
            EMAIL_SUFFIX("email_suffix"),
            URL("url"),
            ROLE_ID("role_id"),
            CHANNEL_ID("channel_id");

            private final String fieldName;
            private final String universalQuery = "UPDATE schools SET " + getFieldName() + " WHERE id = ?";


            SchoolUpdates(String fieldName)
            {
                  this.fieldName = fieldName;
            }

            public String getFieldName()
            {
                  return fieldName;
            }

            public String getUpdateQuery()
            {
                  return universalQuery;
            }
      }

}

