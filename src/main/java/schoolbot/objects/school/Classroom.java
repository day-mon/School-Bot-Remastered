package schoolbot.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.Schoolbot;
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
      private String prerequisite;
      private String instructor;
      private String time;
      private String location;
      private String level;
      private String room;
      private String name;
      private String[] inputClassStartDate;
      private String[] inputClassEndDate;
      private String classIdentifier;
      private String term;
      private String URL;

      private LocalDate startDate;
      private LocalDate endDate;

      private int number;
      private int creditAmount;
      private int id;

      private long roleID;
      private long channelID;
      private long guildID;

      private School school;
      private Professor professor;

      private boolean wasAutoFilled;

      private List<Assignment> assignments;


      public Classroom()
      {
            this.channelID = 0;
            this.roleID = 0;
            this.prerequisite = "None";
            this.description = "N/A";
            assignments = new ArrayList<>();
      }

      public Classroom(long guildId)
      {
            this.guildID = guildId;
            this.channelID = 0;
            this.roleID = 0;
            this.prerequisite = "None";
            this.description = "N/A";
            assignments = new ArrayList<>();
      }


      public Classroom(long channelID, long roleID, String name)
      {
            this.channelID = channelID;
            this.roleID = roleID;
            this.name = name;
      }

      public Classroom(int id, String className)
      {
            this.channelID = 0;
            this.roleID = 0;
            this.id = id;
            this.name = className;
            assignments = new ArrayList<>();
      }


      public Classroom(String description, String time, String location, String classLevel, String room, String className, String classIdentifier, String term, Date startDate, Date endDate, int schoolID, int professorID, int number, int id, long roleID, long channelID, long guildID, School school, Professor professor, boolean wasAutoFilled)
      {
            this.description = description;
            this.time = time;
            this.wasAutoFilled = wasAutoFilled;
            this.location = location;
            this.level = classLevel;
            this.room = room;
            this.name = className;
            this.classIdentifier = classIdentifier;
            this.term = term;
            this.startDate = Instant.ofEpochMilli(startDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            this.endDate = Instant.ofEpochMilli(endDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            this.number = number;
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
            return this.professor.getId();
      }

      public String getInstructor()
      {
            return instructor;
      }

      public int getNumber()
      {
            return number;
      }

      public void setNumber(int number)
      {
            this.number = number;
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

      public String getPrerequisite()
      {
            return prerequisite;
      }

      public void setPrerequisite(String prerequisite)
      {
            this.prerequisite = prerequisite;
      }

      public String getTime()
      {
            return time;
      }

      public void setTime(String time)
      {
            this.time = time;
      }

      public String getLocation()
      {
            return location;
      }

      public void setLocation(String location)
      {
            this.location = location;
      }

      public String getLevel()
      {
            return level;
      }

      public void setLevel(String level)
      {
            this.level = level;
      }

      public String getRoom()
      {
            return room;
      }

      public void setRoom(String room)
      {
            this.room = room;
      }

      public LocalDate getStartDate()
      {
            return startDate;
      }

      public void setStartDate(java.sql.Date startDate)
      {
            this.startDate = Instant.ofEpochMilli(startDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
      }

      public boolean isAutoFilled()
      {
            return wasAutoFilled;
      }

      public void setWasAutoFilled(boolean wasAutoFilled)
      {
            this.wasAutoFilled = wasAutoFilled;
      }

      public LocalDate getEndDate()
      {
            return endDate;
      }

      public void setEndDate(java.sql.Date endDate)
      {
            this.endDate = Instant.ofEpochMilli(endDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
      }

      public String getName()
      {
            return name;
      }

      public void setName(String name)
      {
            this.name = name;
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

      public void addAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            assignments.add(assignment);
            int assignmentID = DatabaseUtil.addAssignment(schoolbot, assignment);

            if (assignmentID == -1)
            {
                  assignments.remove(assignment);
                  return;
            }
            assignment.setId(assignmentID);
            // Times in minutes to remind (1 day, 1 hour, 30 minutes, 10 minutes)
            DatabaseUtil.addAssignmentReminder(schoolbot, assignment, List.of(1440, 60, 30, 10, 0));

      }

      public void removeAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            assignments.remove(assignment);
            DatabaseUtil.removeAssignment(schoolbot, assignment);
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

      public MessageEmbed getAsEmbed(@NotNull Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.roleID);

            return getAsEmbedBuilder(schoolbot)
                    .build();
      }

      public Assignment getAssignmentByID(int id)
      {
            return assignments
                    .stream()
                    .filter(assignment -> assignment.getId() == id)
                    .findFirst()
                    .orElse(null);
      }

      public boolean hasAssignments()
      {
            return !this.assignments.isEmpty();
      }


      public EmbedBuilder getAsEmbedBuilder(@NotNull Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.roleID);
            TextChannel textChannel = schoolbot.getJda().getTextChannelById(this.channelID);

            return new EmbedBuilder()
                    .setTitle(this.name + " | (" + this.classIdentifier + ")", URL)
                    .addField("Class number", String.valueOf(this.number), false)
                    .addField("Meeting time", this.getTime(), false)
                    .addField("Description", this.description, false)
                    .addField("Start Date", this.startDate == null ? Arrays.toString(this.inputClassStartDate) : this.startDate.toString(), false)
                    .addField("End Date", this.endDate == null ? Arrays.toString(this.inputClassEndDate) : this.endDate.toString(), false)
                    .addField("Class ID", String.valueOf(this.id), false)
                    .addField("Professor", this.professor.getFullName(), false)
                    .addField("Role", role == null ? "N/A" : role.getAsMention(), false)
                    .addField("Text Channel", textChannel == null ? "N/A" : textChannel.getAsMention(), false)
                    .addField("Assignments Pending", String.valueOf(this.assignments.size()), false)
                    .setColor(role == null ? Constants.DEFAULT_EMBED_COLOR : role.getColor());
      }
}
