package schoolbot.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.objects.misc.Paginatable;
import schoolbot.util.DatabaseUtil;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Classroom implements Paginatable
{
      private String description;
      private String preReq;
      private String instructor;
      private String classTime;
      private String classLocation;
      private String classLevel;
      private String classRoom;
      private String className;
      private String[] inputClassStartDate;
      private String[] inputClassEndDate;
      private String classIdentifier;
      private String term;
      private String URL;

      private LocalDate classStartDate;
      private LocalDate classEndDate;

      private int classNumber;
      private int creditAmount;
      private int id;

      private long roleID;
      private long channelID;
      private long guildID;

      private School school;
      private Professor professor;

      private final List<Assignment> assignments;


      public Classroom()
      {
            this.channelID = 0;
            this.roleID = 0;
            this.preReq = "None";
            this.description = "N/A";
            assignments = new ArrayList<>();

      }

      public Classroom(int id, String className)
      {
            this.channelID = 0;
            this.roleID = 0;
            this.id = id;
            this.className = className;
            assignments = new ArrayList<>();
      }


      public Classroom(String description, String classTime, String classLocation, String classLevel, String classRoom, String className, String classIdentifier, String term, Date classStartDate, Date classEndDate, int schoolID, int professorID, int classNumber, int id, long roleID, long channelID, long guildID, School school, Professor professor)
      {
            this.description = description;
            this.classTime = classTime;
            this.classLocation = classLocation;
            this.classLevel = classLevel;
            this.classRoom = classRoom;
            this.className = className;
            this.classIdentifier = classIdentifier;
            this.term = term;
            this.classStartDate = Instant.ofEpochMilli(classStartDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            this.classEndDate = Instant.ofEpochMilli(classEndDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            this.classNumber = classNumber;
            this.id = id;
            this.roleID = roleID;
            this.channelID = channelID;
            this.guildID = guildID;
            this.school = school;
            this.professor = professor;
            assignments = new ArrayList<>();
            professor.addClass(this);

      }


      public long getGuildID()
      {
            return guildID;
      }

      public void setGuildID(long guildID)
      {
            this.guildID = guildID;
      }


      public int getProfessorID()
      {
            return this.professor.getID();
      }

      public String getInstructor()
      {
            return instructor;
      }

      public int getClassNumber()
      {
            return classNumber;
      }

      public void setClassNumber(int classNumber)
      {
            this.classNumber = classNumber;
      }

      public int getCreditAmount()
      {
            return creditAmount;
      }

      public void setInstructor(String instructor)
      {
            this.instructor = instructor;
      }

      public void setCreditAmount(int creditAmount)
      {
            this.creditAmount = creditAmount;
      }

      public String[] getInputClassEndDate()
      {
            return inputClassEndDate;
      }

      public void setInputClassEndDate(String[] inputClassEndDate)
      {
            this.inputClassEndDate = inputClassEndDate;
      }

      public String[] getInputClassStartDate()
      {
            return inputClassStartDate;
      }

      public void setInputClassStartDate(String[] inputClassStartDate)
      {
            this.inputClassStartDate = inputClassStartDate;
      }

      public String getDescription()
      {
            return description;
      }

      public void setDescription(String description)
      {
            this.description = description;
      }

      public String getPreReq()
      {
            return preReq;
      }

      public void setPreReq(String preReq)
      {
            this.preReq = preReq;
      }

      public String getClassTime()
      {
            return classTime;
      }

      public void setClassTime(String classTime)
      {
            this.classTime = classTime;
      }

      public String getClassLocation()
      {
            return classLocation;
      }

      public void setClassLocation(String classLocation)
      {
            this.classLocation = classLocation;
      }

      public String getClassLevel()
      {
            return classLevel;
      }

      public void setClassLevel(String classLevel)
      {
            this.classLevel = classLevel;
      }

      public String getClassRoom()
      {
            return classRoom;
      }

      public void setClassRoom(String classRoom)
      {
            this.classRoom = classRoom;
      }

      public LocalDate getClassStartDate()
      {
            return classStartDate;
      }

      public void setClassStartDate(java.sql.Date classStartDate)
      {
            this.classStartDate = Instant.ofEpochMilli(classStartDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
      }

      public LocalDate getClassEndDate()
      {
            return classEndDate;
      }

      public void setClassEndDate(java.sql.Date classEndDate)
      {
            this.classEndDate = Instant.ofEpochMilli(classEndDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
      }

      public String getClassName()
      {
            return className;
      }

      public void setClassName(String className)
      {
            this.className = className;
      }

      public String getClassIdentifier()
      {
            return classIdentifier;
      }

      public void setClassIdentifier(String classIdentifier)
      {
            this.classIdentifier = classIdentifier;
      }

      public long getChannelID()
      {
            return channelID;
      }

      public void setChannelID(long channelID)
      {
            this.channelID = channelID;
      }

      public long getRoleID()
      {
            return roleID;
      }

      public void setRoleID(long roleID)
      {
            this.roleID = roleID;
      }

      public int getSchoolID()
      {
            return getSchool().getID();
      }

      public String getTerm()
      {
            return term;
      }

      public void setTerm(String term)
      {
            this.term = term;
      }

      public School getSchool()
      {
            return school;
      }

      public void setSchool(School school)
      {
            this.school = school;
      }

      public int getId()
      {
            return id;
      }

      public void setId(int id)
      {
            this.id = id;
      }

      public School getSchoolWithoutID()
      {
            return this.school;
      }

      public boolean addAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            assignments.add(assignment);
            int assignmentID = DatabaseUtil.addAssignment(schoolbot, assignment);

            if (assignmentID == -1)
            {
                  assignments.remove(assignment);
                  return false;
            }
            return true;
      }

      public void addAssignment(Assignment assignment)
      {
            assignments.add(assignment);
      }

      public List<Assignment> getAssignments()
      {
            return assignments;
      }

      public void setProfessor(Professor professor)
      {
            this.professor = professor;
      }

      public Professor getProfessor()
      {
            return professor;
      }


      public String getURL()
      {
            return URL;
      }

      public void setURL(String URL)
      {
            this.URL = URL;
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
            TextChannel textChannel = schoolbot.getJda().getTextChannelById(this.channelID);

            return new EmbedBuilder()
                    .setTitle(this.className + " | (" + this.classIdentifier + ")")
                    .addField("Class number", String.valueOf(this.classNumber), false)
                    .addField("Meeting time", this.getClassTime(), false)
                    .addField("Description", this.description, false)
                    .addField("Start Date", this.classStartDate == null ? Arrays.toString(this.inputClassStartDate) : this.classStartDate.toString(), false)
                    .addField("End Date", this.classEndDate == null ? Arrays.toString(this.inputClassEndDate) : this.classEndDate.toString(), false)
                    .addField("Class ID", String.valueOf(this.id), false)
                    .addField("Professor", this.professor.getFullName(), false)
                    .addField("Role", role == null ? "N/A" : role.getAsMention(), false)
                    .addField("Text Channel", textChannel == null ? "N/A" : textChannel.getAsMention(), false)
                    .setColor(role == null ? SchoolbotConstants.DEFAULT_EMBED_COLOR : role.getColor());
      }

}
