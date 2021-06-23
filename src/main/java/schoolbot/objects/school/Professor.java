package schoolbot.objects.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.objects.misc.Paginatable;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class Professor implements Paginatable
{

      private final List<Classroom> listOfClasses;
      private String email;
      private String emailPrefix;
      // Redundant.. will fix..
      private String firstName;
      private String lastName;
      private String fullName;
      private int id;


      private String officeHours;
      private School professorsSchool;

      public Professor()
      {
            this.officeHours = "N/A";
            listOfClasses = new ArrayList<>();

      }

      public Professor(String firstName, String lastName, String emailPrefix)
      {
            this.fullName = firstName;
            this.lastName = lastName;
            this.emailPrefix = emailPrefix;
            this.fullName = firstName + " " + lastName;
            this.officeHours = "N/A";
            listOfClasses = new ArrayList<>();
      }

      public Professor(@NotNull String firstName, @NotNull String lastName, @NotNull String emailPrefix, int id, @NotNull School school)
      {
            this.firstName = firstName;
            this.lastName = lastName;
            this.emailPrefix = emailPrefix;
            this.fullName = firstName + " " + lastName;
            this.professorsSchool = school;
            this.email = emailPrefix + school.getEmailSuffix();
            this.officeHours = "N/A";
            this.id = id;
            listOfClasses = new ArrayList<>();


      }

      public Professor(String firstName, String lastName, School school)
      {
            this.firstName = firstName;
            this.lastName = lastName;
            this.fullName = firstName + " " + lastName;
            this.emailPrefix = lastName.toLowerCase();
            this.professorsSchool = school;
            this.officeHours = "N/A";
            listOfClasses = new ArrayList<>();
      }


      public void setEmail(String email)
      {
            this.email = email;
      }

      public void setFirstName(String firstName)
      {
            if (this.lastName != null)
            {
                  this.firstName = firstName;
                  this.fullName = this.firstName + " " + this.lastName;
            }
            else
            {
                  this.firstName = firstName;
            }
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
            if (this.firstName != null && !this.firstName.isBlank())
            {
                  this.lastName = lastName;
                  this.fullName = this.firstName + " " + this.lastName;
            }
            else
            {
                  this.lastName = lastName;
            }
      }

      public int getSchoolId()
      {
            return getProfessorsSchool().getID();
      }

      public String getEmailPrefix()
      {
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
       * @return String return the officeHours
       */
      public String getOfficeHours()
      {
            return officeHours;
      }

      /**
       * @param officeHours the officeHours to set
       */
      public void setOfficeHours(String officeHours)
      {
            this.officeHours = officeHours;
      }

      public void setEmailPrefix(String emailPrefix)
      {
            this.emailPrefix = emailPrefix;
      }

      /**
       * @return School return the professorsSchool
       */
      public School getProfessorsSchool()
      {
            return professorsSchool;
      }


      public List<Classroom> getListOfClasses()
      {
            return listOfClasses;
      }

      public void addClass(Classroom classroom)
      {
            listOfClasses.add(classroom);
      }

      public void removeClass(Classroom classroom)
      {
            listOfClasses.remove(classroom);
      }

      /**
       * @param professorsSchool the professorsSchool to set
       */
      public void setProfessorsSchool(School professorsSchool)
      {
            this.professorsSchool = professorsSchool;
      }

      public MessageEmbed getAsEmbed(@NotNull Schoolbot schoolbot)
      {
            Role role = schoolbot.getJda().getRoleById(this.professorsSchool.getRoleID());

            return getAsEmbedBuilder(schoolbot)
                    .build();
      }

      public EmbedBuilder getAsEmbedBuilder(@NotNull Schoolbot schoolbot)
      {

            Role role = schoolbot.getJda().getRoleById(this.professorsSchool.getRoleID());
            return new EmbedBuilder()
                    .setTitle("Professor " + lastName)
                    .addField("Professor Name", fullName, false)
                    .addField("Email", getEmail() + " \n ***(These are assumed unless set otherwise)*** ", false)
                    .addField("Office Hours", this.officeHours, false)
                    .addField("Classes Taught", String.valueOf(listOfClasses.size()), false)
                    .addField("Professor ID", String.valueOf(this.id), false)
                    .setColor(role == null ? Color.BLUE : role.getColor())
                    .setTimestamp(Instant.now());
      }


}
