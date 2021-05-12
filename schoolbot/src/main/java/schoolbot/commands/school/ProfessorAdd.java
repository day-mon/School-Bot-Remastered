package schoolbot.commands.school;

import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.Embed;

import java.util.ArrayList;
import java.util.List;

public class ProfessorAdd extends Command
{
    public ProfessorAdd(Command parent)
    {
        super(parent, "Adds a professor to the server list", "[school name] [professor name] [professor email]", 0);
        addFlags(CommandFlag.DATABASE);
    }


    @Override
    public void run(@NotNull CommandEvent event, @NotNull List<String> args)
    {
        List<School> schools = event.getGuildSchools();

        if (schools.isEmpty())
        {
            event.sendMessage("You have no schools to add this professor to... try using -school add first");
            return;
        }
        event.sendMessage("To start.. Whats your professors first name:  ");
        event.getJDA().addEventListener(new ProfessorStateMachine(event, schools));
    }


    public static class ProfessorStateMachine extends ListenerAdapter
    {
        private final long channelId, authorId;
        private final List<School> schools;
        private int state = 0;
        private Schoolbot schoolbot;
        private Professor professor;
        private CommandEvent commandEvent;
        int schoolID = 0;


        public ProfessorStateMachine(CommandEvent event, List<School> schools)
        {
            this.schools = schools;
            this.channelId = event.getChannel().getIdLong();
            this.authorId = event.getUser().getIdLong();
            this.schoolbot = event.getSchoolbot();
            this.commandEvent = event;
            professor = new Professor();
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
                    channel.sendMessageFormat("Awesome! Thank you for that your professors first name is ** %s **", content).queue();
                    professor.setFirstName(content);
                    channel.sendMessage("I will now need your professors last name: ").queue();
                    state = 1;
                }
                case 1 -> {
                    numCheck(content, channel);
                    channel.sendMessageFormat("Thank you again. Your professor last name is ** %s **", content).queue();
                    professor.setLastName(content);
                    professor.setFullName(professor.getFirstName() + " " + professor.getLastName());
                    if (schools.size() == 1)
                    {
                        channel.sendMessageFormat("** %s ** only has one school associated with it. I will automatically assign your professor to  ** %s **", event.getGuild().getName(), schools.get(0).getSchoolName()).queue();
                        professor.setProfessorsSchool(schools.get(0));
                        channel.sendMessage("Lastly, enter his email prefix: ").queue();
                        state = 3;
                        break;
                    }
                    channel.sendMessage("Moving on.. I will need you professors school.. Here is a list of all this servers schools! ").queue();
                    commandEvent.getAsPaginatorWithPageNumbers(commandEvent.getGuildSchools());

                    state = 2;
                }
                case 2 -> {
                    channel.sendMessage("Please choose a page number from the page list above..").queue();
                    if (!Checks.isNumber(content))
                    {
                        Embed.error(commandEvent, "You must give me a number!");
                        return;

                    }

                    if (!between(schools.size()))
                    {
                        Embed.error(event, "** %s ** is not a valid number please choose a number between %d - %d ", content, 1, schools.size());
                        return;
                    }

                    int index = Integer.parseInt(content) - 1;

                    professor.setProfessorsSchool(schools.get(index));
                    channel.sendMessage("""
                            Thank you for that! I will now need your professors email suffix
                            For Ex: **litman**@cs.pitt.edu
                            """).queue();
                    state = 3;

                }
                case 3 -> {
                    professor.setEmailPrefix(content);
                    channel.sendMessage("Thank you.. Inserting all of the info into my database and Adding professor.").queue();

                    if (!commandEvent.addProfessor(commandEvent, professor))
                    {
                        Embed.error(event, "Could not add Professor %s", professor.getLastName());
                        return;
                    }
                    channel.sendMessage(professor.getAsEmbed(commandEvent.getSchoolbot())).queue();

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

        //TODO: Speed running right now will clean this to just check the School List size
        private boolean between(int maxValueInclusive)
        {
            int schoolListSize = schools.size();
            return (schoolListSize >= 1 && schoolListSize <= maxValueInclusive);
        }

        private List<Page> paginate(Schoolbot schoolbot, List<School> list)
        {
            List<Page> pages = new ArrayList<>();
            int i = 1;

            for (School school : list)
            {
                pages.add(new Page(PageType.EMBED,
                        school.getAsEmbedBuilder(schoolbot)
                                .setFooter("Page " + i++ + "/" + list.size())
                                .build()
                ));
            }

            return pages;
        }


    }
}