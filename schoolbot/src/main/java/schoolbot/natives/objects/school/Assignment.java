package schoolbot.natives.objects.school;


import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.OffsetTime;
import java.util.Objects;


public class Assignment implements Comparable<Assignment>
{
    private String name;
    private String description;

    private int points;
    private int professorID;
    private int id;

    private LocalDate dueDate;
    private OffsetTime offsetTime;

    private AssignmentType assignmentType;


    public Assignment()
    {

    }


    public AssignmentType getAssignmentType()
    {
        return assignmentType;
    }

    public void setAssignmentType(AssignmentType assignmentType)
    {
        this.assignmentType = assignmentType;
    }

    public LocalDate getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate)
    {
        this.dueDate = dueDate;
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

    public OffsetTime getOffsetTime()
    {
        return offsetTime;
    }

    public void setOffsetTime(OffsetTime offsetTime)
    {
        this.offsetTime = offsetTime;
    }

    public int compareTo(@NotNull Assignment o)
    {
        if (this.dueDate == dueDate)
        {
            return 1;
        }
        else if (this.dueDate.isBefore(o.dueDate))
        {
            return -1;
        }
        return -1;
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

    public enum AssignmentType
    {
        HOMEWORK("Homework"),
        QUIZ("Quiz"),
        EXAM("Exam"),
        EXTRA_CREDIT("Exam Credit");

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



    

