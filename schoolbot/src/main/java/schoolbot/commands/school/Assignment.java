package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class Assignment extends Command
{
    public Assignment()
    {
        super("Adds, Removes, and Edits Professors to the server list", "[add/edit/remove]", 1);
        addCalls("assign", "assignment");
        addChildren(
                new AssignmentAdd(this),
                new AssignmentRemove(this),
                new AssignmentEdit(this)
        );
    }

    @Override
    public void run(CommandEvent event)
    {

    }
}
