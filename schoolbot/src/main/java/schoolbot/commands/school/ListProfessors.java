package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.util.ArrayList;
import java.util.List;

public class ListProfessors extends Command
{
    public ListProfessors()
    {
        super("", "", 1);
        addCalls("professors", "profs", "lp");
        addFlags(CommandFlag.DATABASE);
    }

    @Override
    public void run(CommandEvent event)
    {
        String arg0 = event.getArgs().get(0);

        if (Checks.isNumber(arg0))
        {
            Embed.error(event, "%s is not a valid school name", arg0);
            return;
        }

        int schoolID = DatabaseUtil.getSchoolID(event.getSchoolbot(), arg0);

        if (schoolID == -1)
        {
            Embed.error(event, "%s does not exist in my database", arg0);
            return;
        }

        List<Professor> professorList = DatabaseUtil.getProfessorsWithClassInformation(event.getSchoolbot(), event.getGuild().getIdLong(), schoolID);

        if (professorList.isEmpty())
        {
            Embed.error(event, "No professors could be found for [ ** %s ** ]", arg0);
            return;
        }

        ArrayList<Page> pages = new ArrayList<>();
        for (Professor p : professorList)
        {
            pages.add(new Page(PageType.EMBED, p.getProfessorAsEmbed()));
        }
        event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent())
                .queue(success -> Pages.paginate(success, pages));

    }
}
