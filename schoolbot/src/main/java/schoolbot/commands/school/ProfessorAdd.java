package schoolbot.commands.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.DatabaseUtil;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class ProfessorAdd extends Command
{
    public ProfessorAdd(Command parent)
    {
        super(parent, "Adds a professor to the server list", "[school name] [professor name] [professor email]", 0);
        addFlags(CommandFlag.DATABASE);

    }

    @Override
    public void run(CommandEvent event)
    {
        List<School> schools = DatabaseUtil.getSchools(event.getSchoolbot());

        if (schools.isEmpty())
        {
            event.sendMessage("You have no schools to add this professor to... try using -school add first");
            return;
        }
        event.sendMessage("To start.. Whats your professors first name:  ");
        event.getJDA().addEventListener(new ProfessorStateMachine(event.getSchoolbot(), event.getChannel(), event.getUser(), schools));
    }


    public static class ProfessorStateMachine extends ListenerAdapter
    {
        private final long channelId, authorId;
        private List<School> schools;
        private static int state = 0;
        private Schoolbot schoolbot;
        String firstName;
        String lastName;
        String emailPrefix;
        String schoolName;
        int schoolID = 0;


        public ProfessorStateMachine(Schoolbot schoolbot, MessageChannel channel, User author, List<School> schools)
        {
            this.schools = schools;
            this.channelId = channel.getIdLong();
            this.authorId = author.getIdLong();
            this.schoolbot = schoolbot;
        }

        public void onGuildMessageReceived(GuildMessageReceivedEvent event)
        {
            if (event.getAuthor().isBot()) return;
            if (event.getChannel().getIdLong() != channelId) return;
            if (event.getAuthor().getIdLong() != authorId) return;
            MessageChannel channel = event.getChannel();
            String content = event.getMessage().getContentRaw();


            switch (state)
            {
                case 0 -> {
                    numCheck(content, channel);
                    channel.sendMessage("Awesome! Thank you for that your professors first name is " + content).queue();
                    firstName = content;
                    channel.sendMessage("I will now need your professors last name: ").queue();
                    state = 1;
                }
                case 1 -> {
                    numCheck(content, channel);
                    channel.sendMessage("Thank you again. Your professor last name is: " + content);
                    lastName = content;
                    if (schools.size() == 1)
                    {
                        channel.sendMessage(event.getGuild().getName() + " only has one school associated with it. I will automatically assign your professor to " + schools.get(0).getSchoolName()).queue();
                        schoolName = schools.get(0).getSchoolName();
                        channel.sendMessage("Lastly, enter his email prefix: ").queue();
                        state = 3;
                        break;
                    }
                    channel.sendMessage("Moving on.. I will need your professors school name: ").queue();
                    state = 2;
                }
                case 2 -> {
                    boolean schoolFound = false;
                    for (School schools : schools)
                    {
                        if (schools.getSchoolName().equalsIgnoreCase(content))
                        {
                            schoolFound = true;
                            break;
                        }
                    }
                    if (schoolFound)
                    {
                        channel.sendMessage("Thank you " + event.getAuthor().getAsMention() + " " + Emoji.SMILEY_FACE.getAsChat() + " your professors school was found!").queue();
                        schoolName = schools.get(0).getSchoolName();
                        schoolID = schools.get(0).getSchoolID();
                        channel.sendMessage("Lastly, enter his email prefix: ").queue();
                        state = 3;
                    }
                    else
                    {
                        channel.sendMessage("School not found.. Aborting").queue();
                        event.getJDA().removeEventListener(this);
                    }
                }
                case 3 -> {
                    emailPrefix = content;
                    channel.sendMessage("Thank you.. Inserting all of the info into my database!").queue();
                    if (DatabaseUtil.addProfessor(schoolbot, firstName, lastName, emailPrefix, schoolID, event.getGuild().getIdLong()))
                    {
                        channel.sendMessage(new EmbedBuilder()
                                .setTitle("Professor Added")
                                .addField("Professor Name", firstName + " " + lastName, false)
                                .addField("School Name", schoolName, false)
                                .addField("Email prefix", emailPrefix, false)
                                .setColor(Color.GREEN)
                                .setTimestamp(Instant.now())
                                .build())
                                .queue();
                    }
                    else
                    {
                        channel.sendMessage("Could not add professor to my database.. contact bot owner").queue();

                    }
                    state = 0;
                    event.getJDA().removeEventListener(this);
                }
            }
        }

        private void numCheck(String content, MessageChannel channel)
        {
            if (content.chars().anyMatch(Character::isDigit))
            {
                channel.sendMessage("Professors fields cannot contain numbers!").queue();
            }
        }

    }
}
