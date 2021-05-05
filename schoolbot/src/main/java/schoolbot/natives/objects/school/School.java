
package schoolbot.natives.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.awt.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class School implements Serializable
{


    private String schoolName;
    private String URL;
    private String emailSuffix;
    private long guildID;
    private long roleID;
    private int schoolID;
    private boolean isPittSchool;


    public School()
    {
        this.roleID = 0L;
        this.schoolName = "N/A";
    }

    public School(String schoolName)
    {
        this.schoolName = schoolName;
        this.roleID = 0L;


    }

    public School(long guildID, String schoolName, String emailSuffix)
    {
        this.schoolName = schoolName;
        this.guildID = guildID;
        this.emailSuffix = emailSuffix;
        this.roleID = 0L;
    }

    public School(String schoolName, long roleID, String emailSuffix, boolean isPittSchool)
    {
        this.schoolName = schoolName;
        this.roleID = roleID;
        this.emailSuffix = emailSuffix;
        this.isPittSchool = isPittSchool;
        this.roleID = 0L;
    }

    public School(int id, String name, long roleID, boolean isPittSchool, String email_suffix, String url)
    {
        this.schoolID = id;
        this.schoolName = name;
        this.roleID = roleID;
        this.isPittSchool = isPittSchool;
        this.emailSuffix = email_suffix;
        this.URL = url;
    }


    /**
     * @return school name
     */
    public String getSchoolName()
    {
        return schoolName;
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

        return new EmbedBuilder()
                .setTitle(this.schoolName, URL)
                .addField("Role", role.getAsMention(), false)
                .addField("Email Suffix", this.emailSuffix, false)
                .addField("School ID", String.valueOf(this.schoolID), false)
                .setColor(role == null ? SchoolbotConstants.DEFAULT_EMBED_COLOR : role.getColor())
                .build();
    }

    public void setURL(String URL)
    {
        this.URL = URL;
    }

    public boolean addClass(GuildMessageReceivedEvent event, Schoolbot schoolbot, String apiURL, Classroom schoolClass)
    {
        MessageChannel channel = event.getChannel();
        Document document = null;

        try
        {
            document = Jsoup.connect(apiURL).get();
        }
        catch (Exception e)
        {
            Embed.error(event, "Could not connect to Peoplesoft.. Try again later!");
            return false;
        }

        if (document.text().contains("Unexpected error occurred."))
        {
            Embed.error(event, "Class information not found.");
            return false;
        }

        // The class name will always be the primary head so we can just automatically it will be a list(0)
        String className = document.getElementsByClass("primary-head").get(0).text();

        // Check if class already exist just in case...
        List<Classroom> classroomList = DatabaseUtil.getClassByClassName(schoolbot, className, guildID);

        if (!classroomList.isEmpty())
        {
            Classroom classroom = classroomList.get(0);
            if (classroom.getClassName().equalsIgnoreCase(schoolName))
            {
                event.getChannel().sendMessageFormat("%s already exist in my database!").queue();
                return false;
            }
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
        schoolClass.setClassName(className);
        schoolClass.setClassNumber(Integer.parseInt(event.getMessage().getContentRaw()));

        for (int left = 2, right = 3; right <= elementsRight.size() - 1; left++, right++)
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
                case "Career" -> {
                    schoolClass.setClassLevel(textRight);
                    break;
                }
                case "Dates" -> {
                    String[] dates = textRight.split("-");
                    String[] start = dates[0].trim().split("/");
                    String[] end = dates[1].trim().split("/");
                    schoolClass.setInputClassEndDate(start);
                    schoolClass.setInputClassStartDate(end);
                    break;
                }
                case "Units" -> {
                    schoolClass.setCreditAmount(Integer.parseInt(textRight.substring(0, 1)));
                    break;
                }
                case "Description" -> {
                    String temp = textRight;
                    if (temp.length() > 1024)
                    {
                        temp = temp.split("\\.")[0] + "....";
                    }
                    schoolClass.setDescription(temp);
                    break;
                }
                case "Enrollment Requirements" -> {
                    schoolClass.setPreReq(textRight);
                    break;
                }
                case "Instructor(s)" -> {
                    List<Professor> professorList = DatabaseUtil.getProfessors(schoolbot, schoolClass.getSchoolID(), guildID);
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
                                found = true;
                                schoolClass.setSchoolID(professor.getSchoolID());
                                schoolClass.setProfessorID(professor.getId());
                                schoolClass.setInstructor(textRight);
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


                        if (DatabaseUtil.addProfessor(schoolbot, firstName, lastName, lastName, schoolClass.getSchoolID(), guildID))
                        {
                            schoolClass.setProfessorID(DatabaseUtil.getProfessorsID(schoolbot, firstName + " " + lastName));
                            schoolClass.setSchoolID(schoolClass.getSchoolID());
                            schoolClass.setInstructor(textRight);
                        }
                        else
                        {
                            schoolbot.getLogger().error("ERROR HAS OCCURRED.... COULD NOT ADD PROFESSOR TO DATABASE!!");
                        }
                    }
                    break;
                }
                case "Meets" -> {
                    schoolClass.setClassTime(textRight);
                    break;
                }
                case "Room" -> {
                    schoolClass.setClassRoom(textRight);
                    break;
                }
                case "Location" -> {
                    schoolClass.setClassLocation(textRight);
                    break;
                }
                case "Status" -> {
                    schoolClass.setClassStatus(textRight);
                    break;
                }
                case "Campus" -> {
                    int length = schoolClass.getSchool().getSchoolName().split("\\s").length;
                    String campus = schoolClass.getSchool().getSchoolName().split("\\s+")[length - 1];
                    String classesCampus = textRight.split("\\s+")[0];
                    if (!campus.toLowerCase().contains(classesCampus.toLowerCase()))
                    {
                        DatabaseUtil.removeProfessor(schoolbot, schoolClass.getProfessorID());

                        Embed.error(event, """
                                You said you goto the ** %s ** campus this class takes place on the ** %s ** campus 
                                Professor %s has been removed 
                                """, campus, classesCampus, schoolClass.getInstructor());
                        return false;
                    }
                    break;
                }
                case "Seats Taken" -> {
                    schoolClass.setSeatsTaken(Integer.parseInt(textRight));
                    break;
                }
                case "Seats Open" -> {
                    schoolClass.setSeatsOpen(Integer.parseInt(textRight));
                    break;
                }
                case "Class Capacity" -> {
                    schoolClass.setClassCapacity(Integer.parseInt(textRight));
                    right = elementsRight.size();
                }
            }
        }

        if (DatabaseUtil.addClassPitt(schoolbot, schoolClass, guildID))
        {
            channel.sendMessage("Class created!").queue();
            channel.sendMessage(new EmbedBuilder()
                    .setTitle(schoolClass.getClassName() + " | (" + schoolClass.getClassIdentifier() + ") ")
                    .addField("Career", schoolClass.getClassLevel(), false)
                    .addField("Credit(s)", String.valueOf(schoolClass.getCreditAmount()), true)
                    .addField("Course Description", schoolClass.getDescription(), false)
                    .addField("Perquisite(s)", schoolClass.getPreReq(), false)
                    .addField("Instructor", schoolClass.getInstructor(), true)
                    .addField("Meeting time", schoolClass.getClassTime(), true)
                    .addField("Campus", schoolClass.getClassLocation(), true)
                    .addField("Room", schoolClass.getClassRoom(), false)
                    .addField("Status", schoolClass.getClassStatus(), false)
                    .addField("Seats", String.valueOf(schoolClass.getSeatsTaken()) + "/" + String.valueOf(schoolClass.getSeatsOpen()), true)
                    .addField("Class Capacity", String.valueOf(schoolClass.getClassCapacity()), true)
                    .setColor(schoolClass.getClassStatus().equals("Open") ? Color.GREEN : Color.RED)
                    .setTimestamp(Instant.now())
                    .build())
                    .queue();
            return true;
        }
        else
        {
            Embed.error(event, """
                    Database failed to add ** %s **
                    """, schoolClass.getClassName());
            return false;
        }
    }


    public void setPittSchool(boolean pittSchool)
    {
        isPittSchool = pittSchool;
    }

    public int getSchoolID()
    {
        return schoolID;
    }

    public void setSchoolID(int schoolID)
    {
        this.schoolID = schoolID;
    }

    public void setRoleID(long roleID)
    {
        this.roleID = roleID;
    }

    public long getRoleID()
    {
        return roleID;
    }

    @Override
    public String toString()
    {
        return "School Name: " + schoolName + "\n" +
                "School email suffix: " + emailSuffix + "\n" +
                "School Role: " + roleID;
    }
}
