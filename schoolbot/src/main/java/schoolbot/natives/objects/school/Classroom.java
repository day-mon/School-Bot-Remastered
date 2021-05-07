package schoolbot.natives.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import schoolbot.Schoolbot;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.util.DatabaseUtil;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class Classroom
{
    private String description;
    private String preReq;
    private String instructor;
    private String classTime;
    private String classLocation;
    private String classLevel;
    private String classRoom;
    private String className;
    private String classStatus;
    private String[] inputClassStartDate;
    private String[] inputClassEndDate;
    private String classIdentifier;
    private String term;
    private String URL;

    private LocalDate classStartDate;
    private LocalDate classEndDate;

    private int seatsTaken;
    private int seatsOpen;
    private int classCapacity;
    private int schoolID;
    private int professorID;
    private int classNumber;
    private int creditAmount;
    private int id;

    private long roleID;
    private long channelID;
    private long guildID;

    private School school;
    private Professor professor;

    private List<Assignment> assignments;


    public Classroom()
    {
        this.preReq = "None";
        this.description = "N/A";
    }

    public Classroom(int id, String className)
    {
        this.id = id;
        this.className = className;
    }


    public Classroom(String description, String classTime, String classLocation, String classLevel, String classRoom, String className, String classIdentifier, String term, Date classStartDate, Date classEndDate, int schoolID, int professorID, int classNumber, int id, long roleID, long channelID, long guildID, School school)
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
        this.schoolID = schoolID;
        this.professorID = professorID;
        this.classNumber = classNumber;
        this.id = id;
        this.roleID = roleID;
        this.channelID = channelID;
        this.guildID = guildID;
        this.school = school;
    }


    public long getGuildID()
    {
        return guildID;
    }

    public void setGuildID(long guildID)
    {
        this.guildID = guildID;
    }

    public void setProfessorID(int professorID)
    {
        this.professorID = professorID;
    }

    public int getProfessorID()
    {
        return professorID;
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

    public String getClassStatus()
    {
        return classStatus;
    }

    public void setClassStatus(String classStatus)
    {
        this.classStatus = classStatus;
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

    public int getSeatsTaken()
    {
        return seatsTaken;
    }

    public void setSeatsTaken(int seatsTaken)
    {
        this.seatsTaken = seatsTaken;
    }

    public int getSeatsOpen()
    {
        return seatsOpen;
    }

    public void setSeatsOpen(int seatsOpen)
    {
        this.seatsOpen = seatsOpen;
    }

    public int getClassCapacity()
    {
        return classCapacity;
    }

    public void setClassCapacity(int classCapacity)
    {
        this.classCapacity = classCapacity;
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
        return schoolID;
    }

    public void setSchoolID(int schoolID)
    {
        this.schoolID = schoolID;
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

    public School getSchool(Schoolbot schoolbot)
    {
        return DatabaseUtil.getSpecificSchoolByID(schoolbot, id, guildID);
    }

    public School getSchoolWithoutID()
    {
        return this.school;
    }

    public boolean addAssignment(Schoolbot schoolbot, Assignment assignment)
    {
        return DatabaseUtil.addAssignment(schoolbot, assignment);
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

    public MessageEmbed getAsEmbedShort(Schoolbot schoolbot)
    {
        Role role = schoolbot.getJda().getRoleById(this.roleID);

        return new EmbedBuilder()
                .setTitle(this.className)
                .addField("Class number", String.valueOf(this.classNumber), false)
                .addField("Description", this.description, false)
                .addField("Start Date", this.classStartDate.toString(), false)
                .addField("End Date", this.classEndDate.toString(), false)
                .addField("Class ID", String.valueOf(this.id), false)
                .setColor(role == null ? SchoolbotConstants.DEFAULT_EMBED_COLOR : role.getColor())
                .build();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Classroom classroom = (Classroom) o;
        return guildID == classroom.guildID && classNumber == classroom.classNumber && creditAmount == classroom.creditAmount && seatsTaken == classroom.seatsTaken && seatsOpen == classroom.seatsOpen && classCapacity == classroom.classCapacity && Objects.equals(description, classroom.description) && Objects.equals(preReq, classroom.preReq) && Objects.equals(instructor, classroom.instructor) && Objects.equals(classTime, classroom.classTime) && Objects.equals(classLocation, classroom.classLocation) && Objects.equals(classLevel, classroom.classLevel) && Objects.equals(classRoom, classroom.classRoom) && Objects.equals(classStatus, classroom.classStatus) && Objects.equals(inputClassStartDate, classroom.inputClassStartDate) && Objects.equals(classIdentifier, classroom.classIdentifier) && Objects.equals(inputClassEndDate, classroom.inputClassEndDate) && Objects.equals(classStartDate, classroom.classStartDate) && Objects.equals(classEndDate, classroom.classEndDate) && Objects.equals(className, classroom.className);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(guildID, classNumber, creditAmount, description, preReq, instructor, classTime, classLocation, classLevel, classRoom, classStatus, inputClassStartDate, classIdentifier, inputClassEndDate, classStartDate, classEndDate, className, seatsTaken, seatsOpen, classCapacity);
    }

    @Override
    public String toString()
    {
        return "Classroom{" +
                "guildID=" + guildID +
                ", classNumber=" + classNumber +
                ", creditAmount=" + creditAmount +
                ", description='" + description + '\'' +
                ", preReq='" + preReq + '\'' +
                ", instructor='" + instructor + '\'' +
                ", classTime='" + classTime + '\'' +
                ", classLocation='" + classLocation + '\'' +
                ", classLevel='" + classLevel + '\'' +
                ", classRoom='" + classRoom + '\'' +
                ", classStatus='" + classStatus + '\'' +
                ", inputClassStartDate='" + inputClassStartDate + '\'' +
                ", classIdentifier='" + classIdentifier + '\'' +
                ", inputClassEndDate='" + inputClassEndDate + '\'' +
                ", classStartDate=" + classStartDate +
                ", classEndDate=" + classEndDate +
                ", className='" + className + '\'' +
                ", seatsTaken=" + seatsTaken +
                ", seatsOpen=" + seatsOpen +
                ", classCapacity=" + classCapacity +
                ", schoolID=" + schoolID +
                ", professorID=" + professorID +
                '}';
    }
}
