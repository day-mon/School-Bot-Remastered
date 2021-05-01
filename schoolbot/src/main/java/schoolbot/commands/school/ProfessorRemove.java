package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class ProfessorRemove extends Command
{
    /**
     *
     * @param parent
     */
    public ProfessorRemove(Command parent)
    {
        super(parent, "", "", 1);
    }


    /**
     *
     * @param event Arguments sent to the command.
     */
    @Override
    public void run(CommandEvent event)
    {
        String arg0 = event.getArgs().get(0);

        // TODO: Do AssignmentAdd
    }
}
