package schoolbot.commands.school;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.util.List;
import java.util.stream.Collectors;

public class ListSchools extends Command
{
    public ListSchools(EventWaiter waiter)
    {
        super("List schools all schools in database", "[none]", 0);
        addCalls("schools", "school-list");

    }

    @Override
    public void run(CommandEvent event)
    {
        List<School> schools = DatabaseUtil.getSchools(event.getSchoolbot())
                .stream()
                .filter(school -> event.getGuild().getIdLong() == school.getGuildID())
                .collect(Collectors.toList());

        if (schools.isEmpty())
        {
            Embed.error(event, "No schools!");
            return;
        }


        StringBuilder s2 = new StringBuilder("```");

        for (School s : schools)
        {
            s2.append(s.toString()).append("\n");
        }

        // schools.forEach(schoolsInList -> s2.append(schools.toString()).append("========== \n"));


        s2.append("```");

        event.sendMessage(s2.toString());


    }
}
