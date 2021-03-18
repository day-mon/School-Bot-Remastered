package schoolbot.natives.objects.school;

import java.io.Serializable;
import java.util.HashMap;

import javax.management.relation.RoleList;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.entities.GuildImpl;
import schoolbot.natives.objects.school.*;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.objects.school.Student;

import schoolbot.natives.objects.school.Assignment;
import schoolbot.natives.objects.school.Professor;

public class Classroom implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6721439396011219354L;
    private transient GuildImpl guild;
    private String classID;
    private String classNum;
    private String className;
    private transient Role role = null;
    private String textChannel;
    private long textChannelID;
    private String time;
    private String year;
    private String subject;
    private int credits;
    private Professor professor;
    private School school;
    private HashMap<String, Student> classList;
    private HashMap<String, Assignment> assignments;
    private int[] intervals;

    public Classroom() {

    }

    public Classroom(GuildImpl guild, String className, String classID, String classNum, String time, int credits,
                     Professor professor, School school, String textChannel, long textChannelID) {
        this.guild = guild;
        this.classID = classID;
        this.time = time;
        this.school = school;
        this.className = className;
        this.professor = professor;
        this.classNum = classNum;
        this.textChannelID = textChannelID;
        this.credits = credits;
        classList = new HashMap<>();
        assignments = new HashMap<>();
        this.textChannel = textChannel;
        professor.addClass(this);
    }

    public String getClassID() {
        return this.classID;
    }

    public String getClassName() {
        return className;
    }

    public String getSubject() {
        return subject;
    }

    public School getSchool() {
        return this.school;
    }

    public HashMap<String, Student> getClassList() {
        return classList;
    }

    public Role getRole() {
        return this.role;
    }

    public int getCredits() {
        return credits;
    }

    public GuildImpl getGuild() {
        return guild;
    }

    public int[] getIntervals() {
        return intervals;
    }

    public Professor getProfessor() {
        return this.professor;
    }

    public String getTime() {
        return this.time;
    }

    public String getTextChannel() {
        return textChannel;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public void setClassList(HashMap<String, Student> classList) {
        this.classList = classList;
    }

    public void setIntervals(int[] intervals) {
        intervals = new int[intervals.length];
        this.intervals = intervals;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public void setTextChannel(String textChannel) {
        this.textChannel = textChannel;
    }

    public void setSchool(School school) {
        this.school = school;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void addStudent(Student student) {
        classList.putIfAbsent(student.getRealName(), student);
    }

    public boolean removeStudent(Student student) {
        if (classList.containsKey(student.getRealName())) {
            classList.remove(student.getRealName());
            return true;
        }
        return false;
    }

    public void addToAllStudents(Assignment assignment) {
        for (Student students : classList.values()) {
            students.addAssignment(assignment);
        }
    }

    public boolean containsStudent(Student student) {
        return classList.containsValue(student);

    }

    public boolean containsAssignment(String assignment) {
        return assignments.containsKey(assignment);
    }

    public void addAssignment(Assignment assignemnt) {
        assignments.putIfAbsent(assignemnt.getAssignmentName(), assignemnt);
    }

    public boolean removeAssignment(String assignemnt) {
        if (assignments.containsKey(assignemnt)) {
            assignments.remove(assignemnt);
            return true;
        }
        return false;
    }

    public long getTextChannelID() {
        return textChannelID;
    }

    public void setTextChannelID(long textChannelID) {
        this.textChannelID = textChannelID;
    }

    /**
     * @return String return the classNum
     */
    public String getClassNum() {
        return classNum;
    }

    public HashMap<String, Assignment> getAssignments() {
        return this.assignments;
    }

    public void setAssignments(HashMap<String, Assignment> assignments) {
        this.assignments = assignments;
    }

    /**
     * @param classNum the classNum to set
     */
    public void setClassNum(String classNum) {
        this.classNum = classNum;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Classroom other = (Classroom) obj;
        if (assignments == null) {
            if (other.assignments != null)
                return false;
        } else if (!assignments.equals(other.assignments))
            return false;
        if (classID == null) {
            if (other.classID != null)
                return false;
        } else if (!classID.equals(other.classID))
            return false;
        if (classList == null) {
            if (other.classList != null)
                return false;
        } else if (!classList.equals(other.classList))
            return false;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (classNum == null) {
            if (other.classNum != null)
                return false;
        } else if (!classNum.equals(other.classNum))
            return false;
        if (credits != other.credits)
            return false;
        if (guild == null) {
            if (other.guild != null)
                return false;
        } else if (!guild.equals(other.guild))
            return false;
        if (professor == null) {
            if (other.professor != null)
                return false;
        } else if (!professor.equals(other.professor))
            return false;
        if (school == null) {
            if (other.school != null)
                return false;
        } else if (!school.equals(other.school))
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        if (time == null) {
            if (other.time != null)
                return false;
        } else if (!time.equals(other.time))
            return false;
        if (year == null) {
            if (other.year != null)
                return false;
        } else if (!year.equals(other.year))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Class Name: " + className + "\n" 
                + "ClassID: " + classID + "\n" 
                + "Class size: " + classList.size() + "\n" 
                + "ClassNum: " + classNum + "\n" 
                + "Channel: " + textChannel + "\n"
                + "Credits: " + credits + "\n" 
                + "Professor: "+ professor.getLastName() + ", " + professor.getFirstName() + "\n" 
                + "Time: " + time + "\n"
                + "======================";

    }

}
