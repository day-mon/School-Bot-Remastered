package schoolbot.natives.objects.school;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;


@Deprecated
public class Student
{

      /**
     * School this student is attending.
     */
    private School mySchool;
    /**
     * List of classes this student is in.
     */
    private HashMap<String, Classroom> myClasses;

    /**
     * List of assignments and status of those assignwemnts
     */
    private HashMap<Assignment, Boolean> assignments;

    /**
     * Student's GPA.
     */
    private double GPA;
    /**
     * List of student's majors/minors
     *
     */
    /**
     * Student's real name
     */
    private String realName;

    /**
     * Students email prefix
     */
    private String emailPrefix;


    /**
     *
     */
    private User studentUser;

    /**
     *
     * @param guild
     * @param user
     */

    /**
     * @param user User account.
     */
    public Student(User user)
    {

        this.studentUser = user;
        this.myClasses = null;
        this.mySchool = null;
        this.GPA = -1.0;
        this.realName = "N/A";
        assignments = new HashMap<>();

    }

    public Student(User user, School mySch, double GPA, String realName)
    {
        this.studentUser = user;
        this.myClasses = new HashMap<>();
        this.mySchool = mySch;
        this.GPA = GPA;
        this.realName = realName;
        assignments = new HashMap<>();
    }

    /**
     * Remove a class from this student's schedule.
     *
     * @param clazz Class ({@code Classroom}) to remove
     * @return
     */


      /**
       * @param major Major to add to this student's list
       */


      public void addAssignment(Assignment assignment)
    {
        assignments.putIfAbsent(assignment, false);
    }

    public boolean removeAssignment(Assignment assignment)
    {
        if (assignments.containsKey(assignment))
        {
            assignments.remove(assignment);
            return true;
        }
        return false;
    }

    // #region GETTER SETTERS
    // -------------------------------------------------------
    public School getSchool()
    {
        return mySchool;
    }

    public void setSchool(School mySchool)
    {
        this.mySchool = mySchool;
    }

    public HashMap<String, Classroom> getClasses()
    {
        return myClasses;
    }

    public void setClasses(HashMap<String, Classroom> myClasses)
    {
        this.myClasses = myClasses;
    }

    public double getGPA()
    {
        return GPA;
    }

    public String getEmailPrefix()
    {
        return emailPrefix;
    }

    public User getSUser()
    {
        return this.studentUser;
    }

    public void setGPA(double gPA)
    {
        GPA = gPA;
    }


    public String getRealName()
    {
        return realName;
    }

    public void setEmailPrefix(String emailPrefix)
    {
        this.emailPrefix = emailPrefix;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }
    // #endregion
    // ----------------------------------------------------------------------

    /**
     * Saves this instance to its respective save file by writing a comma-separated
     * line of the variable values.
     *
     * @implNote Variables are written out in order they appear in class.
     */
    public void save()
    {
        // pass
    }

    /**
     * Get this student's data as a pretty MessageEmbed.
     *
     * @return {@code MessageEmbed} to send in discord messages.
     */
    public MessageEmbed getAsEmbed()
    {
        return null; // this fat cock will be from
        // {JDA/src/main/java/net/dv8tion/jda/api/entities/MessageEmbed.java}
    }

    // DEFAULT OVERRIDES
    @Override
    public String toString()
    {
        return "Name: " + realName + "\n" + "School" + mySchool.getSchoolName() + "\n" + "GPA: " + GPA + "\n" + "Contact: " + emailPrefix + mySchool.getEmailSuffix() + "\n"
                + "Amount of classes" + myClasses.size() + "\n" + "-------------------------------------------------";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(GPA);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((myClasses == null) ? 0 : myClasses.hashCode());
        result = prime * result + ((mySchool == null) ? 0 : mySchool.hashCode());
        result = prime * result + ((realName == null) ? 0 : realName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Student other = (Student) obj;
        if (Double.doubleToLongBits(GPA) != Double.doubleToLongBits(other.GPA))
        {
            return false;
        }
        if (myClasses == null)
        {
            if (other.myClasses != null)
            {
                return false;
            }
        }
        else if (!myClasses.equals(other.myClasses))
        {
            return false;
        }
        if (mySchool == null)
        {
            if (other.mySchool != null)
            {
                return false;
            }
        }
        else if (!mySchool.equals(other.mySchool))
        {
            return false;
        }
        if (realName == null)
        {
            if (other.realName != null)
            {
                return false;
            }
        }
        else if (!realName.equals(other.realName))
        {
            return false;
        }
        return true;
    }

}
