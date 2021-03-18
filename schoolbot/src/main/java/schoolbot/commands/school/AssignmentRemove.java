package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class AssignmentRemove extends Command
{
    /**
     *
     * @param parent
     */
    public AssignmentRemove(Command parent)
    {
        super(parent, " ", " ", 1);
    }


    /**
     *
     * @param event Arguments sent to the command.
     */
    @Override
    public void run(CommandEvent event)
    {

    }
}
