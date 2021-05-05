package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;

public class ClassroomRemove extends Command
{

    public ClassroomRemove(Command parent)
    {
        super("Removes a class from a school", "", 0);
        addPermissions(Permission.ADMINISTRATOR);
        addFlags(CommandFlag.DATABASE);

    }

    /**
     * What the command will do on call.
     *
     * @param event Arguments sent to the command.
     */
    @Override
    public void run(CommandEvent event)
    {

    }
}
