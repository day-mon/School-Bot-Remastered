package schoolbot.natives.objects.school;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;


public class Assignment implements Serializable, Comparable<Assignment> {

    /**
     *
     */
    private static final long serialVersionUID = -1874828658877582324L;
    private Professor assignedProfessor;
    private Classroom clazz;
    private Date dueDate;
    private LocalDateTime ldt;
    private String assignmentName;
    private double pointsAmount;
    private String assignmentRef;
    private boolean expired = false;
    private int[] intervals;

    public Assignment() 
    {

    }

    public Assignment(Classroom clazz, String assignmentName, Date dueDate, double pointsAmount, String assigmentType) 
    {
        this.clazz = clazz;
        this.assignmentName = assignmentName;
        this.pointsAmount = pointsAmount;
        this.dueDate = dueDate;
        this.assignmentRef = assigmentType;
        assignedProfessor = clazz.getProfessor();
        ldt = LocalDateTime.now();

    }



    public Professor getAssignedProfessor() {
        return assignedProfessor;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public String getAssignmentRef() {
        return assignmentRef;
    }

    public Classroom getClazz() {
        return clazz;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public double getPointsAmount() {
        return pointsAmount;
    }

    public LocalDateTime getLdt() {
        return ldt;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean e) {
        expired = e;
    }


    public void setAssignedProfessor(Professor assignedProfessor) {
        this.assignedProfessor = assignedProfessor;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }


    public void setIntervals(int[] intervals) {
        intervals = new int[intervals.length];
        this.intervals = intervals;
    }

    public void setClazz(Classroom clazz) {
        this.clazz = clazz;
    }

    public void setAssignmentRef(String assignmentRef) {
        this.assignmentRef = assignmentRef;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setPointsAmount(double pointsAmount) {
        this.pointsAmount = pointsAmount;
    }

    @Override
    public String toString() 
    {
        return  "Assignment Name: " + assignmentName + "\n" +
                "Assignment Point Amount: " + pointsAmount + "\n" +
                "Due Date: " + dueDate + "\n";
    }


    @Override
	public int compareTo(Assignment o) 
    {
		if (this.dueDate == o.dueDate) {
            return 0;
            } else if (this.dueDate.before(o.dueDate)) {
                return -1;
            }
            return 1;
        }
        
    }
    

