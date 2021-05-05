package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.util.ArrayList;
import java.util.List;

public class ListSchools extends Command
{
    public ListSchools()
    {
        super("List schools all schools in database", "[none]", 0);
        addCalls("schools", "school-list");
        addFlags(CommandFlag.DATABASE);

    }

    @Override
    public void run(CommandEvent event)
    {
        List<School> schools = DatabaseUtil.getSchools(event.getSchoolbot(), event.getGuild().getIdLong());

        if (schools.isEmpty())
        {
            Embed.error(event, "No schools!");
            return;
        }

        ArrayList<Page> pages = new ArrayList<>();

        for (School s : schools)
        {
            pages.add(new Page(PageType.EMBED, s.getAsEmbed(event.getSchoolbot())));
        }

        event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success ->
                Pages.paginate(success, pages));


    }
}
