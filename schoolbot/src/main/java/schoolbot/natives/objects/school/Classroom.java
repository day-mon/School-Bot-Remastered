package schoolbot.natives.objects.school;

import java.time.LocalDateTime;
import java.util.Objects;

public class Classroom
{

    private long guildID;
    private int classNumber;
    private int creditAmount;
    private String description;
    private String preReq;
    private String instructor;
    private String classTime;
    private String classLocation;
    private String classLevel;
    private String classRoom;
    private String classStatus;
    private String schoolName;
    private String inputClassStartDate;
    private String classIdentifier;
    private String inputClassEndDate;
    private LocalDateTime classStartDate;
    private LocalDateTime classEndDate;
    private String className;
    private int seatsTaken;
    private int seatsOpen;
    private int classCapacity;
    private int schoolID;
    private int professorID;
    private int id;
    private long roleID;
    private long channelID;


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


    public Classroom(int classNumber, int creditAmount, String description, String preReq, String classTime, String classLocation, String classLevel, String classRoom, String classStatus, String schoolName, LocalDateTime classStartDate, LocalDateTime classEndDate, String className, int seatsTaken, int seatsOpen, int classCapacity)
    {
        this.classNumber = classNumber;
        this.creditAmount = creditAmount;
        this.description = description;
        this.preReq = preReq;
        this.classTime = classTime;
        this.classLocation = classLocation;
        this.classLevel = classLevel;
        this.classRoom = classRoom;
        this.classStatus = classStatus;
        this.schoolName = schoolName;
        this.classStartDate = classStartDate;
        this.classEndDate = classEndDate;
        this.className = className;
        this.seatsTaken = seatsTaken;
        this.seatsOpen = seatsOpen;
        this.classCapacity = classCapacity;
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

    public String getInputClassEndDate()
    {
        return inputClassEndDate;
    }

    public void setInputClassEndDate(String inputClassEndDate)
    {
        this.inputClassEndDate = inputClassEndDate;
    }

    public String getInputClassStartDate()
    {
        return inputClassStartDate;
    }

    public void setInputClassStartDate(String inputClassStartDate)
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

    public String getSchoolName()
    {
        return schoolName;
    }

    public void setSchoolName(String schoolName)
    {
        this.schoolName = schoolName;
    }

    public LocalDateTime getClassStartDate()
    {
        return classStartDate;
    }

    public void setClassStartDate(LocalDateTime classStartDate)
    {
        this.classStartDate = classStartDate;
    }

    public LocalDateTime getClassEndDate()
    {
        return classEndDate;
    }

    public void setClassEndDate(LocalDateTime classEndDate)
    {
        this.classEndDate = classEndDate;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Classroom classroom = (Classroom) o;
        return guildID == classroom.guildID && classNumber == classroom.classNumber && creditAmount == classroom.creditAmount && seatsTaken == classroom.seatsTaken && seatsOpen == classroom.seatsOpen && classCapacity == classroom.classCapacity && Objects.equals(description, classroom.description) && Objects.equals(preReq, classroom.preReq) && Objects.equals(instructor, classroom.instructor) && Objects.equals(classTime, classroom.classTime) && Objects.equals(classLocation, classroom.classLocation) && Objects.equals(classLevel, classroom.classLevel) && Objects.equals(classRoom, classroom.classRoom) && Objects.equals(classStatus, classroom.classStatus) && Objects.equals(schoolName, classroom.schoolName) && Objects.equals(inputClassStartDate, classroom.inputClassStartDate) && Objects.equals(classIdentifier, classroom.classIdentifier) && Objects.equals(inputClassEndDate, classroom.inputClassEndDate) && Objects.equals(classStartDate, classroom.classStartDate) && Objects.equals(classEndDate, classroom.classEndDate) && Objects.equals(className, classroom.className);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(guildID, classNumber, creditAmount, description, preReq, instructor, classTime, classLocation, classLevel, classRoom, classStatus, schoolName, inputClassStartDate, classIdentifier, inputClassEndDate, classStartDate, classEndDate, className, seatsTaken, seatsOpen, classCapacity);
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
                ", schoolName='" + schoolName + '\'' +
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
