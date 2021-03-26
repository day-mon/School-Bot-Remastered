package schoolbot.commands.school;

import com.jagrosh.jdautilities.command.annotation.JDACommand;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.Menu;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.exceptions.PermissionException;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.School;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        List<School> schools = event.getSchoolbot().getDatabaseHandler().getSchools();


        StringBuffer s2 = new StringBuffer("```");


        for (School s : schools)
        {

            s2.append(s.toString() + "\n");
        }

        s2.append("```");

        event.sendMessage(s2.toString());


    }
}
