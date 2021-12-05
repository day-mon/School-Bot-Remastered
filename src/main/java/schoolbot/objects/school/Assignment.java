package schoolbot.objects.school;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import schoolbot.Constants;
import schoolbot.Schoolbot;
import schoolbot.objects.misc.interfaces.Paginatable;
import schoolbot.objects.misc.interfaces.Remindable;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class Assignment implements Comparable<Assignment>, Paginatable, Remindable
{
      private String name;
      private String description;

      private int points;
      private int professorID;
      private int id;

      private LocalDateTime dueDate;

      private AssignmentType type;
      private Classroom classroom;


      public Assignment()
      {

      }

      public Assignment(String name, String description, int points, int professorID, int id, String type, Timestamp timestamp, Classroom classroom)
      {
            this.name = name;
            this.description = description;
            this.points = points;
            this.professorID = professorID;
            this.id = id;
            this.dueDate = timestamp.toLocalDateTime();
            this.type =
                    switch (type)
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

      public AssignmentType getType()
      {
            return type;
      }

      public void setType(AssignmentType type)
      {
            this.type = type;
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


      public LocalDateTime getDueDate()
      {
            return dueDate;
      }

      public void setDueDate(LocalDateTime dueDate)
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
            this.professorID = classroom.getProfessorID();
      }

      @Override
      public boolean equals(Object o)
      {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            Assignment that = (Assignment) o;
            return points == that.points && professorID == that.professorID && id == that.id && Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(dueDate, that.dueDate) && type == that.type;
      }

      @Override
      public int hashCode()
      {
            return Objects.hash(name, description, points, professorID, id, dueDate, type);
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
                   ", assignmentType=" + type +
                   '}';
      }

      @Override
      public MessageEmbed getAsEmbed(@NotNull Schoolbot schoolbot)
      {
            return getAsEmbedBuilder(schoolbot)
                    .build();
      }

      @Override
      public EmbedBuilder getAsEmbedBuilder(@NotNull Schoolbot schoolbot)
      {
            var role = schoolbot.getJda().getRoleById(this.classroom.getRoleID());
            var builder = new StringBuilder("");
            if (description.length() >= 1024)
            {
                  builder.append(description, 0, 1021)
                          .append("...");
            }

            return new EmbedBuilder()
                    .setTitle(this.name)
                    .addField("Description", builder.equals("") ? description  : builder.toString() , false)
                    .addField("Type", type.getAssignmentType(), false)
                    .addField("Points", String.valueOf(this.points), false)
                    .addField("Class", classroom.getName(), false)
                    .addField("Due Date", this.dueDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm")), false)
                    .setColor(role == null ? Constants.DEFAULT_EMBED_COLOR : role.getColor())
                    .setTimestamp(Instant.now());

      }

      public enum AssignmentType
      {
            HOMEWORK("Homework"),
            QUIZ("Quiz"),
            EXAM("Exam"),
            EXTRA_CREDIT("Extra Credit"),
            PAPER("PAPER");

            private final String assignmentType;

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



    

