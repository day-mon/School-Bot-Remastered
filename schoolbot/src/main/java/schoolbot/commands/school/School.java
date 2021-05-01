package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class School extends Command
{
    public School()
    {
        super("Adds, Removes, and Edits schools", "[add/edit/remove]", 0);
        addCalls("school", "university");

        addChildren(
                new SchoolAdd(this),
                new SchoolEdit(this),
                new SchoolRemove(this)
        );
    }

    @Override
    public void run(CommandEvent event)
    {

    }
}