package schoolbot.commands.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.awt.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public class ClassroomAdd extends Command
{
    public ClassroomAdd(Command parent)
    {
        super(parent, "", "", 0);
        addFlags(CommandFlag.DATABASE);


    }

    @Override
    public void run(CommandEvent event)
    {
        event.sendMessage("Do you attend a University of Pittsburgh Campus ? ");
        event.getJDA().addEventListener(new ClassAddStateMachine(event.getSchoolbot(), event.getChannel(), event.getUser()));
    }

    public static class ClassAddStateMachine extends ListenerAdapter
    {
        private final long channelID, authorID;
        private int state = 1;
        private List<School> schools;
        private Schoolbot schoolbot;
        private String CLASS_SEARCH_URL = "https://psmobile.pitt.edu/app/catalog/classsection/UPITT/";
        Classroom schoolClass;
        School school;
        String schoolName;

        public ClassAddStateMachine(Schoolbot schoolbot, MessageChannel channel, User author)
        {
            this.channelID = channel.getIdLong();
            this.authorID = author.getIdLong();
            this.schoolbot = schoolbot;
            this.schools = DatabaseUtil.getSchools(schoolbot);

        }

        @Override
        public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
        {
            if (event.getAuthor().isBot()) return;
            if (event.getAuthor().getIdLong() != authorID) return;
            if (event.getChannel().getIdLong() != channelID) return;

            long guildID = event.getGuild().getIdLong();
            MessageChannel channel = event.getChannel();
            String message = event.getMessage().getContentRaw();

            switch (state)
            {
                case 1 -> {
                    schoolClass = new Classroom();
                    schoolClass.setGuildID(guildID);
                    if (schools == null || schools.isEmpty())
                    {
                        Embed.error(event, "There are no schools for the server ");
                        return;
                    }
                    if (message.equalsIgnoreCase("Yes") || message.equalsIgnoreCase("y"))
                    {
                        boolean isDown = false;
                        try
                        {
                            isDown = Jsoup.connect("https://psmobile.pitt.edu/app/catalog/classSearch").get().text().contains("PeopleSoft Monthly Maintenance in Progress");
                        }
                        catch (Exception e)
                        {
                            isDown = true;
                            e.printStackTrace();
                        }

                        if (isDown)
                        {
                            Embed.error(event, "People soft is currently down for maintenance **OR** I could not connect to PeopleSoft");
                            event.getJDA().removeEventListener(this);
                            return;
                        }
                    }
                    channel.sendMessage("What campus do you attend. (Main, Johnstown, Titusville, or Bradford): ").queue();
                    state = 2;
                }
                case 2 -> {
                    for (School schoolInDb : schools)
                    {
                        String potSchoolName = schoolInDb.getSchoolName();
                        if (potSchoolName.contains("University of Pittsburgh"))
                        {
                            if (message.equalsIgnoreCase("main"))
                            {
                                if (potSchoolName.equalsIgnoreCase("University of Pittsburgh"))
                                {
                                    channel.sendMessageFormat("Coolio.. This server has %s as a school. We can now continue.", potSchoolName).queue();
                                    channel.sendMessageFormat("Pog sauce %s I will now need your term\n Here are some valid entries: `Fall 2020, Summer 2019, Spring 2021`", Emoji.SMILEY_FACE.getAsChat()).queue();
                                    schoolClass.setSchoolName(potSchoolName);
                                    schoolClass.setSchoolID(schoolInDb.getSchoolID());
                                    state = 3;
                                    return;
                                }
                            }
                            else if (potSchoolName.toLowerCase().contains(message.toLowerCase()))
                            {
                                channel.sendMessageFormat("Coolio.. This server has %s as a school. We can now continue.", potSchoolName).queue();
                                channel.sendMessageFormat("Pog sauce %s I will now need your term\n Here are some valid entries: `Fall 2020, Summer 2019, Spring 2021`", Emoji.SMILEY_FACE.getAsChat()).queue();
                                schoolClass.setSchoolName(potSchoolName);
                                schoolClass.setSchoolID(schoolInDb.getSchoolID());
                                state = 3;
                                return;
                            }
                        }
                    }
                    Embed.error(event, "School could not be found. Please use the [school add] command to add the school to this server");
                    state = 1;
                    event.getJDA().removeEventListener(this);
                    return;
                }
                case 3 -> {
                    int term = termValidator(message);
                    if (term == -1)
                    {
                        Embed.error(event, "Not a valid term. Aborting..\n" +
                                "Reason for Aborting\n" +
                                "1. **Term is either to old or too far ahead in the future**\n" +
                                "2. **You mistyped the term**\n" +
                                "3. **You did not input a valid season**");
                        event.getJDA().removeEventListener(this);
                        state = 1;
                        break;
                    }
                    CLASS_SEARCH_URL += term + "/";
                    channel.sendMessage("What is your class #\n Hint: This can normally be found on your syllabus, psmobile or peoplesoft, or in the link of your class ").queue();
                    state = 4;
                    return;
                }
                case 4 -> {
                    CLASS_SEARCH_URL += message;
                    Document document = null;
                    try
                    {
                        document = Jsoup.connect(CLASS_SEARCH_URL).get();
                    }
                    catch (Exception e)
                    {
                        Embed.error(event, "Could not connect to Peoplesoft.. Try again later!");
                        event.getJDA().removeEventListener(this);
                        return;
                    }

                    //Checks if class is even a valid number
                    if (document.text().contains("Unexpected error occurred."))
                    {
                        Embed.error(event, "Class information not found.");
                        state = 1;
                        event.getJDA().removeEventListener(this);
                        break;
                    }


                    // The class name will always be the primary head so we can just automatically it will be a list(0)
                    String className = document.getElementsByClass("primary-head").get(0).text();

                    // Check if class already exist just in case...
                    List<Classroom> classroomList = DatabaseUtil.getClassByClassName(schoolbot, className, guildID);

                    if (!classroomList.isEmpty())
                    {
                        Classroom classroom = classroomList.get(0);
                        if (classroom.getClassName().equalsIgnoreCase(schoolName))
                        {
                            channel.sendMessageFormat("%s already exist in my database!").queue();
                            event.getJDA().removeEventListener(this);
                            return;
                        }
                    }


                    // All Elements on left side of class page
                    Elements elementsLeft = document.getElementsByClass("pull-left");

                    // All Elements on right side of class page
                    Elements elementsRight = document.getElementsByClass("pull-right");

                    // Identifier for class Ex: CS 0015, COMP 101;
                    String[] identifier = document.getElementsByClass("page-title  with-back-btn").get(0).text().split("\\s");

                    // Parsing the subject and the class number
                    String subject = identifier[0];
                    String classNameNum = identifier[identifier.length - 1];
                    String subjectAndClassNameAndNum = subject + " " + classNameNum;


                    // Setting classname and identifier
                    schoolClass.setClassIdentifier(subjectAndClassNameAndNum);
                    schoolClass.setClassName(className);
                    schoolClass.setClassNumber(Integer.parseInt(message));


                    for (int left = 2, right = 3; right <= elementsRight.size() - 1; left++, right++)
                    {

                        // Gotta check if the current tag w e are on is a div because if its not we are not on something we wanna scrape.
                        while (!elementsRight.get(right).tag().getName().equalsIgnoreCase("div"))
                        {
                            right++;
                        }

                        // Text on left side of class page (i.e Description, Class Times, etc);
                        String textLeft = elementsLeft.get(left).text();
                        // Text on right side of class page (i.e actual data)
                        String textRight = elementsRight.get(right).text();


                        switch (textLeft)
                        {
                            case "Career" -> {
                                schoolClass.setClassLevel(textRight);
                                break;
                            }
                            case "Dates" -> {
                                schoolClass.setInputClassEndDate(textRight.split("-")[0].trim());
                                schoolClass.setInputClassStartDate(textRight.split("-")[1].trim());
                                break;
                            }
                            case "Units" -> {
                                schoolClass.setCreditAmount(Integer.parseInt(textRight.substring(0, 1)));
                                break;
                            }
                            case "Description" -> {
                                String temp = textRight;
                                if (temp.length() > 1024)
                                {
                                    temp = temp.split("\\.")[0] + "....";
                                }
                                schoolClass.setDescription(temp);
                                break;
                            }
                            case "Enrollment Requirements" -> {
                                schoolClass.setPreReq(textRight);
                                break;
                            }
                            case "Instructor(s)" -> {
                                List<Professor> professorList = DatabaseUtil.getProfessors(schoolbot, schoolClass.getSchoolID(), guildID);
                                boolean found = false;
                                Professor professorFound = null;
                                if (!professorList.isEmpty())
                                {
                                    for (Professor professor : professorList)
                                    {
                                        String firstAndLast = professor.getFirstName() + " " + professor.getLastName();
                                        if (firstAndLast.equalsIgnoreCase(textRight))
                                        {
                                            // Could change the found flag to just if professorFound == null
                                            found = true;
                                            schoolClass.setSchoolID(professor.getSchoolID());
                                            schoolClass.setProfessorID(professor.getId());
                                            schoolClass.setInstructor(textRight);
                                            break;
                                        }
                                    }
                                }


                                if (!found)
                                {
                                    channel.sendMessage("This professor has not been found in my database for this server... adding him now!").queue();
                                    int length = textRight.split("\\s+").length;

                                    String firstName = textRight.split("\\s+")[0];
                                    String lastName = (length < 2) ? "Unknown" : textRight.split("\\s+")[1];


                                    if (DatabaseUtil.addProfessor(schoolbot, firstName, lastName, lastName, schoolClass.getSchoolID(), guildID))
                                    {
                                        schoolClass.setProfessorID(DatabaseUtil.getProfessorsID(schoolbot, firstName + " " + lastName));
                                        schoolClass.setSchoolID(schoolClass.getSchoolID());
                                        schoolClass.setInstructor(textRight);
                                    }
                                    else
                                    {
                                        schoolbot.getLogger().error("ERROR HAS OCCURRED.... COULD NOT ADD PROFESSOR TO DATABASE!!");
                                    }
                                }
                                break;
                            }
                            case "Meets" -> {
                                schoolClass.setClassTime(textRight);
                                break;
                            }
                            case "Room" -> {
                                schoolClass.setClassRoom(textRight);
                                break;
                            }
                            case "Location" -> {
                                schoolClass.setClassLocation(textRight);
                                break;
                            }
                            case "Status" -> {
                                schoolClass.setClassStatus(textRight);
                                break;
                            }
                            case "Campus" -> {
                                int length = schoolClass.getSchoolName().split("\\s").length;
                                String campus = schoolClass.getSchoolName().split("\\s+")[length - 1];
                                String classesCampus = textRight.split("\\s+")[0];
                                if (!campus.toLowerCase().contains(classesCampus.toLowerCase()))
                                {
                                    DatabaseUtil.removeProfessor(schoolbot, schoolClass.getProfessorID());

                                    Embed.error(event, """
                                            You said you goto the ** %s ** campus this class takes place on the ** %s ** campus 
                                            Professor %s has been removed 
                                            """, campus, classesCampus, schoolClass.getInstructor());
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                break;
                            }
                            case "Seats Taken" -> {
                                schoolClass.setSeatsTaken(Integer.parseInt(textRight));
                                break;
                            }
                            case "Seats Open" -> {
                                schoolClass.setSeatsOpen(Integer.parseInt(textRight));
                                break;
                            }
                            case "Class Capacity" -> {
                                schoolClass.setClassCapacity(Integer.parseInt(textRight));
                                right = elementsRight.size();
                            }
                        }
                    }

                    if (DatabaseUtil.addClassPitt(schoolbot, schoolClass, guildID))
                    {
                        channel.sendMessage("Class created!").queue();
                        channel.sendMessage(new EmbedBuilder()
                                .setTitle(schoolClass.getClassName() + " | (" + schoolClass.getClassIdentifier() + ") ")
                                .addField("Career", schoolClass.getClassLevel(), false)
                                .addField("Credit(s)", String.valueOf(schoolClass.getCreditAmount()), true)
                                .addField("Course Description", schoolClass.getDescription(), false)
                                .addField("Perquisite(s)", schoolClass.getPreReq(), false)
                                .addField("Instructor", schoolClass.getInstructor(), true)
                                .addField("Meeting time", schoolClass.getClassTime(), true)
                                .addField("Campus", schoolClass.getClassLocation(), true)
                                .addField("Room", schoolClass.getClassRoom(), false)
                                .addField("Status", schoolClass.getClassStatus(), false)
                                .addField("Seats", String.valueOf(schoolClass.getSeatsTaken()) + "/" + String.valueOf(schoolClass.getSeatsOpen()), true)
                                .addField("Class Capacity", String.valueOf(schoolClass.getClassCapacity()), true)
                                .setColor(schoolClass.getClassStatus().equals("Open") ? Color.GREEN : Color.RED)
                                .setTimestamp(Instant.now())
                                .build())
                                .queue();
                    }
                    else
                    {
                        Embed.error(event, """
                                Database failed to add ** %s **
                                """, schoolClass.getClassName());
                    }
                    event.getJDA().removeEventListener(this);

                }

                /* TODO: Make it optional to add a role and text channel
                case 5 -> {
                    channel.sendMessage(new EmbedBuilder()
                            .setColor(new Random().nextInt(0xFFFFF))
                            .setTitle("One last thing...")
                            .setDescription("You have 4 Options.. Pick a number 1-4 below")
                            .addField("Option (1)", "Create a role and a TextChannel", false)
                            .addField("Option (2)", "Create a role with not TextChannel", true)
                            .addField("Option (3)", "Create a TextChannel no role", false)
                            .addField("Option (4)", "Create no role ", true)
                            .build());

                    if (Checks.allMatchesNumber(message))
                    {
                        int number = Integer.parseInt(message);

                        if (number <= 1 || number >= 4)
                        {
                            Embed.error(event, "%s is not a valid entry... try again!", message);
                            state = 5;
                            return;
                        }

                        switch (number)
                        {
                            case 1 -> {
                                long id = 23;
                                Consumer<Guild> guildConsumer = (guild -> {
                                    guild.createRole()
                                            .setName("s")
                                            .queue();
                                });

                            }
                            case 2 -> {

                            }
                            case 3 -> {

                            }
                            case 4 -> {

                            }
                        }

                    }
                }

                case 6 -> {

                }
*/
            }


        }
    }

    private static int termValidator(String content)
    {
        Map<String, Integer> map = Map.of("fall", 1, "spring", 4, "summer", 7);

        if (content.split("\\s+").length != 2) return -1;

        String season = content.split("\\s")[0].toLowerCase();
        String yearString = content.split("\\s")[1];

        if (yearString.length() != 4) return -1;
        if (!season.chars().allMatch(Character::isLetter)) return -1;
        if (!yearString.chars().anyMatch(Character::isDigit)) return -1;

        // Computer Generated
        int curYear = LocalDateTime.now().getYear();
        int millennium = curYear / 1000;
        int trailingYear = curYear % 100;

        //User
        int yearInt = Integer.parseInt(yearString);
        int userMillennium = yearInt / 1000;
        int userTrailingYear = (season.equalsIgnoreCase("fall")) ? (yearInt % 100) + 1 : (yearInt % 100);
        if (!season.equalsIgnoreCase("spring") && !season.equalsIgnoreCase("summer") && !season.equalsIgnoreCase("fall"))
            return -1;
        if (millennium != userMillennium) return -1;
        if (userTrailingYear == trailingYear || userTrailingYear == trailingYear + 1) ;
        else return -1;

        int term = (((userMillennium * 100) + userTrailingYear) * 10) + map.get(season);
        return term;
    }
}