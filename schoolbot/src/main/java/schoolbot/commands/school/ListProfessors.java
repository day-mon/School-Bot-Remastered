package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;

public class ListProfessors extends Command
{
    public ListProfessors()
    {
        super("", "", 1);
        addCalls("professors");
        addFlags(CommandFlag.DATABASE);
    }

    @Override
    public void run(CommandEvent event)
    {

        List<Professor> professors = DatabaseUtil.getProfessors(event.getSchoolbot(), event.getArgs().get(0), event.getGuild().getIdLong());

        ArrayList<Page> pages = new ArrayList<>();
        MessageBuilder mb = new MessageBuilder();

        for (Professor p : professors)
        {
            mb.clear();
            pages.add(new Page(PageType.EMBED, p.getProfessorAsEmbed()));
        }

        event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success ->
                Pages.paginate(success, pages));


    }
}
