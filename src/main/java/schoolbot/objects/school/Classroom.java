package schoolbot.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.Schoolbot;
import schoolbot.objects.misc.interfaces.Paginatable;
import schoolbot.objects.misc.interfaces.Remindable;
import schoolbot.util.DatabaseUtils;
import schoolbot.util.Parser;
import schoolbot.util.StringUtils;

import java.sql.Date;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Classroom implements Paginatable, Remindable
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
      private final List<Assignment> assignments;
      private String classIdentifier;
      private String term;
      private String URL;

      private LocalDateTime startDate;
      private LocalDateTime endDate;

      private int number;
      private int creditAmount;
      private int id;

      private long roleID;
      private long channelID;
      private long guildID;

      private School school;
      private Professor professor;
      private String inputTime;
      private boolean wasAutoFilled = false;


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


      public Classroom(long channelID, long roleID, String name, Date startDate, Date endDate, String time)
      {
            this.channelID = channelID;
            this.roleID = roleID;
            this.name = name;
            this.startDate = Parser.parseTimeString(startDate, time);
            this.endDate = Parser.parseTimeString(endDate, time);
            assignments = new ArrayList<>();

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
            this.startDate = Parser.parseTimeString(startDate, time);
            this.endDate = Parser.parseTimeString(endDate, time);
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
            return startDate.toLocalDate();
      }

      public LocalDateTime getStartDateWithTime()
      {
            return startDate;
      }

      public void setStartDate(java.sql.Date startDate)
      {
            var startDatee = Instant.ofEpochMilli(startDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (this.startDate != null)
            {
                  this.startDate = LocalDateTime.of(startDatee, this.startDate.toLocalTime());
            }
            else
            {
                  this.startDate = LocalDateTime.of(startDatee, LocalTime.of(0, 0));
            }
      }

      public void setStartDate(LocalDateTime localDateTime)
      {
            this.startDate = localDateTime;
      }

      public void setTime(LocalTime time)
      {
            this.startDate = startDate.toLocalDate().atTime(time);
            this.endDate = endDate.toLocalDate().atTime(time);
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
            return endDate.toLocalDate();
      }

      public LocalDateTime getEndDateWithTime()
      {
            return endDate;
      }

      public void setEndDate(java.sql.Date endDate)
      {

            var endDatee = Instant.ofEpochMilli(endDate.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            if (this.endDate != null)
            {
                  this.endDate = LocalDateTime.of(endDatee, this.endDate.toLocalTime());
            }
            else
            {
                  this.endDate = LocalDateTime.of(endDatee, LocalTime.of(0, 0));

            }
      }

      public List<Assignment> getAssignmentsOnDate(LocalDate date)
      {
            return assignments.stream()
                    .filter(assignment -> assignment.getDueDate().toLocalDate().isEqual(date))
                    .collect(Collectors.toList());
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

      public boolean hasChannel()
      {
            return channelID != 0;
      }

      public void setChannelID(long channelID)
      {
            this.channelID = channelID;
      }

      public long getRoleID()
      {
            return roleID;
      }

      public boolean hasRole()
      {
            return roleID != 0;
      }


      public void setRoleID(long roleID)
      {
            this.roleID = roleID;
      }

      public int getSchoolID()
      {
            return school.getID();
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

      public String getInputTime()
      {
            return inputTime;
      }

      public void setInputTime(String inputTime)
      {
            this.inputTime = inputTime;
      }


      public void addAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            assignments.add(assignment);
            int assignmentID = DatabaseUtils.addAssignment(schoolbot, assignment);

            if (assignmentID == -1)
            {
                  assignments.remove(assignment);
                  return;
            }
            assignment.setId(assignmentID);
            // Times in minutes to remind (1 day, 1 hour, 30 minutes, 10 minutes, and when due)
            DatabaseUtils.addAssignmentReminder(schoolbot, assignment, List.of(1440, 60, 30, 10, 0));

      }

      public void removeAssignment(Schoolbot schoolbot, Assignment assignment)
      {
            assignments.remove(assignment);
            DatabaseUtils.removeAssignment(schoolbot, assignment);
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
            var role = schoolbot.getJda().getRoleById(this.roleID);
            var textChannel = schoolbot.getJda().getTextChannelById(this.channelID);

            if (description.length() > Constants.MAX_FIELD_VALUE)
            {
                  this.description = "Description too long to display";
            }

            return new EmbedBuilder()
                    .setTitle(this.name + " | (" + this.classIdentifier + ")", URL)
                    .addField("Class number", String.valueOf(this.number), false)
                    .addField("Meeting time", this.time, false)
                    .addField("Room", this.room == null ? "N/A" : this.room, false)
                    .addField("Description", this.description, false)
                    .addField("Start Date", this.startDate == null ? Arrays.toString(this.inputClassStartDate) : StringUtils.formatDate(this.startDate), false)
                    .addField("End Date", this.endDate == null ? Arrays.toString(this.inputClassEndDate) : StringUtils.formatDate(this.endDate), false)
                    .addField("Class ID", String.valueOf(this.id), false)
                    .addField("Professor", this.professor.getFullName(), false)
                    .addField("Role", role == null ? "N/A" : role.getAsMention(), false)
                    .addField("Text Channel", textChannel == null ? "N/A" : textChannel.getAsMention(), false)
                    .addField("Assignments Pending", String.valueOf(this.assignments.size()), false)
                    .setColor(role == null ? Constants.DEFAULT_EMBED_COLOR : role.getColor());
      }

      @Override
      public String toString()
      {
            return "Classroom{" +
                   "description='" + description + '\'' +
                   ", prerequisite='" + prerequisite + '\'' +
                   ", instructor='" + instructor + '\'' +
                   ", time='" + time + '\'' +
                   ", location='" + location + '\'' +
                   ", level='" + level + '\'' +
                   ", room='" + room + '\'' +
                   ", name='" + name + '\'' +
                   ", inputClassStartDate=" + Arrays.toString(inputClassStartDate) +
                   ", inputClassEndDate=" + Arrays.toString(inputClassEndDate) +
                   ", assignments=" + assignments +
                   ", classIdentifier='" + classIdentifier + '\'' +
                   ", term='" + term + '\'' +
                   ", URL='" + URL + '\'' +
                   ", startDate=" + startDate +
                   ", endDate=" + endDate +
                   ", number=" + number +
                   ", creditAmount=" + creditAmount +
                   ", id=" + id +
                   ", roleID=" + roleID +
                   ", channelID=" + channelID +
                   ", guildID=" + guildID +
                   ", school=" + school +
                   ", professor=" + professor +
                   ", inputTime='" + inputTime + '\'' +
                   ", wasAutoFilled=" + wasAutoFilled +
                   '}';
      }

      @Override
      public boolean equals(Object o)
      {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Classroom classroom = (Classroom) o;
            return this.id == classroom.id;
      }

      @Override
      public int hashCode()
      {
            int result = Objects.hash(description, prerequisite, instructor, time, location, level, room, name, assignments, term, URL, startDate, endDate, number, creditAmount, id, roleID, channelID, guildID, school, professor, inputTime, wasAutoFilled);
            result = 31 * result + Arrays.hashCode(inputClassStartDate);
            result = 31 * result + Arrays.hashCode(inputClassEndDate);
            return result;
      }
}
