package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.util.List;

public class SchoolRemove extends Command
{
    public SchoolRemove(Command parent)
    {
        super(parent, "Removes a school given the name", "[school name]", 1);
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void run(CommandEvent event)
    {
        String arg0 = event.getArgs().get(0);
        if (Checks.isNumber(arg0))
        {
            Embed.error(event, "School names cannot contain numbers!");
            return;
        }
        List<School> schools = DatabaseUtil.getSchools(event.getSchoolbot());
        if (schools.size() == 0)
        {
            Embed.error(event, "There are no schools for " + event.getGuild().getName());
            return;
        }

        School schoolToRemove = null;
        for (School school : schools)
        {
            if (school.getSchoolName().equals(arg0))
            {
                schoolToRemove = school;
                break;
            }
        }

        if (schoolToRemove == null)
        {
            Embed.error(event, arg0 + " does not exist");
            return;
        }

        if (DatabaseUtil.removeSchool(event.getSchoolbot(), arg0))
        {
            if (event.getJDA().getRoleById(schoolToRemove.getRoleID()) != null)
            {
                event.getJDA().getRoleById(schoolToRemove.getRoleID()).delete().queue();
            }
            Embed.confirmation(event, "School successfully deleted!");
        }
        else
        {
            Embed.error(event, "School could not be removed!");
        }


    }
}
