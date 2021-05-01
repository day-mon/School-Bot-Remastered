package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

public class SchoolRemove extends Command
{
    public SchoolRemove(Command parent)
    {
        super(parent, "Removes a school given the name", "[school name]", 1);
        addPermissions(Permission.ADMINISTRATOR);
        addFlags(CommandFlag.DATABASE);
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

        int schoolID = DatabaseUtil.getSchoolID(event.getSchoolbot(), arg0);

        if (schoolID == -1)
        {
            Embed.error(event, " ** %s ** does not exist!", arg0);
            return;
        }

        boolean hasClasses = DatabaseUtil.getClassesBySchoolID(event.getSchoolbot(), schoolID);

        if (hasClasses)
        {
            Embed.error(event, "** %s ** could not be deleted because it has classes assigned to it", arg0);
            return;
        }

        School schoolToRemove = DatabaseUtil.getSpecificSchoolByID(event.getSchoolbot(), schoolID, event.getGuild().getIdLong());

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
