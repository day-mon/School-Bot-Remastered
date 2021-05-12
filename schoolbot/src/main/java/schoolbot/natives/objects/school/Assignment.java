package schoolbot.natives.objects.school;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.misc.Paginatable;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;


public class Assignment implements Comparable<Assignment>, Paginatable
{
      private String name;
      private String description;

      private int points;
      private int professorID;
      private int id;

      private OffsetDateTime dueDate;

      private AssignmentType assignmentType;
      private Classroom classroom;


      public Assignment()
      {

      }

      public Assignment(String name, String description, int points, int professorID, int id, String assignmentType, Timestamp timestamp, Classroom classroom)
      {
            this.name = name;
            this.description = description;
            this.points = points;
            this.professorID = professorID;
            this.id = id;
            this.dueDate = OffsetDateTime.of(timestamp.toLocalDateTime().toLocalDate(), LocalTime.from(timestamp.toLocalDateTime()), ZoneOffset.UTC);
            this.assignmentType = switch (assignmentType)
                    {
                          case "Exam":
                                yield AssignmentType.EXAM;
                          case "Quiz":
                                yield AssignmentType.QUIZ;
                          case "Extra Credit":
                                yield AssignmentType.EXTRA_CREDIT;
                          case "Paper":
                                yield AssignmentType.PAPER;
                          default:
                                yield AssignmentType.HOMEWORK;
                    };
            this.classroom = classroom;
      }

      public AssignmentType getAssignmentType()
      {
            return assignmentType;
      }

      public void setAssignmentType(AssignmentType assignmentType)
      {
            this.assignmentType = assignmentType;
      }


      public int getPoints()
      {
            return points;
      }

      public void setPoints(int points)
      {
            this.points = points;
      }

      public String getName()
      {
            return name;
      }

      public void setName(String name)
      {
            this.name = name;
      }

      public int getProfessorID()
      {
            return professorID;
      }

      public void setProfessorID(int professorID)
      {
            this.professorID = professorID;
      }

      public int getId()
      {
            return id;
      }

      public void setId(int id)
      {
            this.id = id;
      }

      public String getDescription()
      {
            return description;
      }

      public void setDescription(String description)
      {
            this.description = description;
      }


      public OffsetDateTime getDueDate()
      {
            return dueDate;
      }

      public void setDueDate(OffsetDateTime dueDate)
      {
            this.dueDate = dueDate;
      }

      public int compareTo(@NotNull Assignment o)
      {
            if (this.dueDate.isEqual(o.dueDate))
            {
                  return 1;
            }
            else if (this.dueDate.isBefore(o.dueDate))
            {
                  return -1;
            }
            return -1;
      }

      public Classroom getClassroom()
      {
            return classroom;
      }

      public void setClassroom(Classroom classroom)
      {
            this.classroom = classroom;
      }

      @Override
      public boolean equals(Object o)
      {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Assignment that = (Assignment) o;
            return points == that.points && professorID == that.professorID && id == that.id && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(dueDate, that.dueDate) && assignmentType == that.assignmentType;
      }

      @Override
      public int hashCode()
      {
            return Objects.hash(name, description, points, professorID, id, dueDate, assignmentType);
      }

      @Override
      public String toString()
      {
            return "Assignment {" +
                   "name='" + name + '\'' +
                   ", description='" + description + '\'' +
                   ", points=" + points +
                   ", professorID=" + professorID +
                   ", id=" + id +
                   ", dueDate=" + dueDate +
                   ", assignmentType=" + assignmentType +
                   '}';
      }

      @Override
      public MessageEmbed getAsEmbed(Schoolbot schoolbot)
      {
            return getAsEmbedBuilder(schoolbot)
                    .build();
      }

      @Override
      public EmbedBuilder getAsEmbedBuilder(Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.classroom.getRoleID());
            return new EmbedBuilder()
                    .setTitle(this.name)
                    .addField("Description", this.description, false)
                    .addField("Type", assignmentType.getAssignmentType(), false)
                    .addField("Points", String.valueOf(this.points), false)
                    .addField("Class", classroom.getClassName(), false)
                    .addField("Professor", this.classroom.getProfessor().getFullName(), false)
                    .setColor(role == null ? SchoolbotConstants.DEFAULT_EMBED_COLOR : role.getColor())
                    .setTimestamp(Instant.now());

      }

      public enum AssignmentType
      {
            HOMEWORK("Homework"),
            QUIZ("Quiz"),
            EXAM("Exam"),
            EXTRA_CREDIT("Exam Credit"),
            PAPER("PAPER");

            String assignmentType;

            AssignmentType(String assignmentType)
            {
                  this.assignmentType = assignmentType;
            }

            public String getAssignmentType()
            {
                  return assignmentType;
            }
      }
}



    

