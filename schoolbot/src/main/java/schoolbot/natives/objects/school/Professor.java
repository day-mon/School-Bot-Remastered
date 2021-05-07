package schoolbot.natives.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;


public class Professor implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3024391926657713863L;
    private List<Classroom> listOfClasses;
    private String email;
    private String emailPrefix;
    // Redundant.. will fix..
    private String firstName;
    private String lastName;
    private String fullName;
    private int age;
    private int schoolID;
    private int id;
    private int classCount;


    private String officeHours;
    private School professorsSchool;

    public Professor()
    {
        classCount = 0;
    }

    public Professor(String firstName, String lastName, String emailPrefix)
    {
        this.fullName = firstName;
        this.lastName = lastName;
        this.emailPrefix = emailPrefix;
        this.fullName = firstName + " " + lastName;
        classCount = 0;
    }

    public Professor(String firstName, String lastName, String emailPrefix, int id, School school)
    {
        this.fullName = firstName;
        this.lastName = lastName;
        this.emailPrefix = emailPrefix;
        this.fullName = firstName + " " + lastName;
        this.professorsSchool = school;
        this.id = id;
        classCount = 0;

    }

    public Professor(String firstName, String lastName, School school)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.lastName = lastName.toLowerCase();
        this.professorsSchool = school;
    }


    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setClassCount(int classCount)
    {
        this.classCount = classCount;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }


    public void setSchoolID(int schoolID)
    {
        this.schoolID = schoolID;
    }

    public int getSchoolID()
    {
        return schoolID;
    }



    public String getEmailPrefix() {
        return emailPrefix;
    }

    public String getEmail()
    {
        return email + professorsSchool.getEmailSuffix();
    }


    public String getLastName()
    {
        return lastName;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }


    /**
     * @return String return the name
     */
    public String getFirstName()
    {
        return firstName;
    }



    /**
     * @return int return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * @param age the age to set
     */
    public void setAge(int age) {
        this.age = age;
    }

    /**
     * @return String return the officeHours
     */
    public String getOfficeHours() {
        return officeHours;
    }

    /**
     * @param officeHours the officeHours to set
     */
    public void setOfficeHours(String officeHours) {
        this.officeHours = officeHours;
    }

    public void setEmailPrefix(String emailPrefix) {
        this.emailPrefix = emailPrefix;
    }

    /**
     * @return School return the professorsSchool
     */
    public School getProfessorsSchool()
    {
        return professorsSchool;
    }


    public int increaseClassCount()
    {
        return this.classCount += 1;

    }

    public void addClass(Classroom classroom)
    {
        listOfClasses.add(classroom);
    }

    /**
     * @param professorsSchool the professorsSchool to set
     */
    public void setProfessorsSchool(School professorsSchool)
    {
        this.professorsSchool = professorsSchool;
    }

    public MessageEmbed getAsEmbed()
    {
        return new EmbedBuilder()
                .setTitle("Professor " + lastName)
                .addField("Professor Name", fullName, false)
                .addField("Email prefix", emailPrefix + " \n ***(These are assumed unless set otherwise)*** ", false)
                .addField("Classes Taught", "" + classCount, false)
                .setColor(Color.blue)
                .setTimestamp(Instant.now())
                .build();
    }

    public EmbedBuilder getAsEmbedBuilder()
    {
        return new EmbedBuilder()
                .setTitle("Professor " + lastName)
                .addField("Professor Name", fullName, false)
                .addField("Email prefix", emailPrefix + " \n **(These are assumed unless set otherwise)** ", false)
                .addField("Classes Taught", "" + classCount, false)
                .setColor(Color.blue)
                .setTimestamp(Instant.now());
    }


}
