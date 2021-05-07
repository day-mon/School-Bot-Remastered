package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Checks;
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

        if (!event.schoolExist(arg0))
        {
            Embed.error(event, " ** %s ** does not exist!", arg0);
            return;
        }

        School school = event.getSchool(event, arg0);

        if (school.getClassroomList().size() > 0 || school.getProfessorList().size() > 0)
        {
            Embed.error(event, "** %s ** could not be deleted because it has classes or professors assigned to it", arg0);
            return;
        }


        event.removeSchool(event, school);

        Embed.success(event, "** %s ** successfully deleted", school.getSchoolName());

    }
}
