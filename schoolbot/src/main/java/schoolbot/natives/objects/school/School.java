package schoolbot.natives.objects.school;

import java.awt.Color;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.internal.entities.GuildImpl;

public class School implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7699216426153116210L;
    private String schoolName;
    private Role serverRole;
    private String schoolreference;
    private String emailSuffix;
    private transient GuildImpl guild;
    private HashMap<String, Classroom> listOfClasses;
    private HashMap<String, Student> listOfStudents;
    private HashMap<String, Professor> listOfProfessors;

    public School() {

    }

    public School(String schoolName) {
        this.schoolName = schoolName;
        listOfClasses = new HashMap<>();
        listOfProfessors = new HashMap<>();
        listOfStudents = new HashMap<>();

    }

    public School(GuildImpl guild, String schoolName, String emailSuffix, String schoolreference) {
        this.schoolName = schoolName;
        this.guild = guild;
        this.emailSuffix = emailSuffix;
        this.schoolreference = schoolreference;
        listOfClasses = new HashMap<>();
        listOfProfessors = new HashMap<>();
        listOfStudents = new HashMap<>();
    }

    public School(String schoolname, String emailSuffix, HashMap<String, Classroom> listOfClasses,
            HashMap<String, Student> listOfStudents) {
        this.schoolName = schoolname;
        this.emailSuffix = emailSuffix;
        this.listOfClasses = listOfClasses;
        this.listOfStudents = listOfStudents;
    }

    /**
     * Getters & Setters
     */

    /**
     * @return list of all classes at the university
     */
    public HashMap<String, Classroom> getListOfClasses() {
        return listOfClasses;
    }

    /**
     * 
     * @return school name
     */
    public String getSchoolName() {
        return schoolName;
    }

    public HashMap<String, Professor> getListOfProfessors() {
        return listOfProfessors;
    }

    public String getEmailSuffix() {
        return emailSuffix;
    }

    public String getSchoolreference() {
        return schoolreference;
    }

    public GuildImpl getGuild() {
        return guild;
    }

    public double getAverageGPA() {
        int avg = 0;
        for (Student student : listOfStudents.values()) {
            double num = student.getGPA();
            if (num > 0) {
                avg += num;
            }
        }

        return avg;
    }

    public void setSchoolreference(String schoolreference) {
        this.schoolreference = schoolreference;
    }

    /**
     * 
     * @return
     */
    public HashMap<String, Student> getListOfStudents() {
        return listOfStudents;
    }

    /**
     * 
     * @param listOfClasses
     */
    public void setListOfClasses(HashMap<String, Classroom> listOfClasses) {
        this.listOfClasses = listOfClasses;
    }

    /**
     * 
     * @param listOfStudents
     */
    public void setListOfStudents(HashMap<String, Student> listOfStudents) {
        this.listOfStudents = listOfStudents;
    }

    /**
     * 
     * @param schoolName
     */
    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setEmailSuffix(String emailSuffix) {
        this.emailSuffix = emailSuffix;
    }

    public void setListOfProfessors(HashMap<String, Professor> listOfProfessors) {
        this.listOfProfessors = listOfProfessors;
    }

    public void addStudent(Student student) {
        listOfStudents.putIfAbsent(student.getRealName(), student);
    }

    public boolean removeStudent(Student student) {
        if (!listOfStudents.containsKey(student.getRealName()))
            return false;
        else
            listOfClasses.remove(student.getRealName());
        return true;

    }

    public void addClazz(Classroom clazz) {
        listOfClasses.putIfAbsent(clazz.getClassNum(), clazz);
    }

    public boolean removeClazz(Classroom clazz) {
        if (!listOfClasses.containsKey(clazz.getClassNum()))
            return false;
        else
            listOfClasses.remove(clazz.getClassNum());
        return true;
    }

    public void addProfessor(Professor prof) {
        listOfProfessors.putIfAbsent(prof.getEmailPrefix(), prof);
    }

    public boolean removeProfessor(Professor prof) {
        if (!listOfProfessors.containsKey(prof.getEmailPrefix()))
            return false;
        else
            listOfProfessors.remove(prof.getEmailPrefix());
        return true;

    }

    public EmbedBuilder getAsEmbed() {
        Date dateGenerated = new Date();
        EmbedBuilder pretyifyEmbed = new EmbedBuilder();
        pretyifyEmbed.setTitle(":books: " + captializer(schoolName) + " :books:");
        pretyifyEmbed.setColor(Color.blue);
        pretyifyEmbed.setTitle(":books: University Information :books:");
        pretyifyEmbed.setDescription("School name: " + schoolName + "\n" + "Number of students: " + listOfClasses.size()
                + "\n" + "Number of classes: " + listOfStudents.size());
        pretyifyEmbed.setFooter("Generated on: " + dateGenerated.getMonth() + "/" + dateGenerated.getDay() + "/"
                + dateGenerated.getYear());
        return pretyifyEmbed;

    }

    private String captializer(String str) {
        // if string is null or empty, return empty string
        if (str == null || str.length() == 0)
            return "";

        /*
         * if string contains only one char, make it capital and return
         */
        if (str.length() == 1)
            return str.toUpperCase();

        /*
         * Split the string by space
         */
        String[] words = str.split(" ");

        // create empty StringBuilder with same length as string
        StringBuilder sbCapitalizedWords = new StringBuilder(str.length());

        /*
         * For each word, 1. get first character using substring 2. Make it upper case
         * and append to string builder 3. append the rest of the characters as-is to
         * string builder 4. append space to string builder
         */
        for (String word : words) {

            if (word.length() > 1)
                sbCapitalizedWords.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
            else
                sbCapitalizedWords.append(word.toUpperCase());

            sbCapitalizedWords.append(" ");
        }

        /*
         * convert StringBuilder to string, also remove last space from it using trim
         * method
         */
        return sbCapitalizedWords.toString().trim();
    }

    @Override
    public String toString() {
        return "School Name: " + schoolName + "\n" + "School email suffix: " + emailSuffix + "\n"
                + "Amount of Classes: " + listOfClasses.size() + "\n" + "Amount of Professors: "
                + listOfProfessors.size() + "\n" + "Average GPA: " + getAverageGPA();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((emailSuffix == null) ? 0 : emailSuffix.hashCode());
        result = prime * result + ((listOfClasses == null) ? 0 : listOfClasses.hashCode());
        result = prime * result + ((listOfProfessors == null) ? 0 : listOfProfessors.hashCode());
        result = prime * result + ((listOfStudents == null) ? 0 : listOfStudents.hashCode());
        result = prime * result + ((schoolName == null) ? 0 : schoolName.hashCode());
        result = prime * result + ((serverRole == null) ? 0 : serverRole.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        School other = (School) obj;
        if (emailSuffix == null) {
            if (other.emailSuffix != null)
                return false;
        } else if (!emailSuffix.equals(other.emailSuffix))
            return false;
        if (listOfClasses == null) {
            if (other.listOfClasses != null)
                return false;
        } else if (!listOfClasses.equals(other.listOfClasses))
            return false;
        if (listOfProfessors == null) {
            if (other.listOfProfessors != null)
                return false;
        } else if (!listOfProfessors.equals(other.listOfProfessors))
            return false;
        if (listOfStudents == null) {
            if (other.listOfStudents != null)
                return false;
        } else if (!listOfStudents.equals(other.listOfStudents))
            return false;
        if (schoolName == null) {
            if (other.schoolName != null)
                return false;
        } else if (!schoolName.equals(other.schoolName))
            return false;
        if (serverRole == null) {
            if (other.serverRole != null)
                return false;
        } else if (!serverRole.equals(other.serverRole))
            return false;
        return true;
    }

}
