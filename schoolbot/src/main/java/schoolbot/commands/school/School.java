package schoolbot.commands.school;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import schoolbot.commands.school.SchoolAdd;
import schoolbot.commands.school.SchoolEdit;
import schoolbot.commands.school.SchoolRemove;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class School extends Command
{
    private final EventWaiter waiter;
    public School(EventWaiter waiter)
    {
        super("Adds, Removes, and Edits schools", "[add/edit/remove]" , 0);
        addCalls("school", "university");
        this.waiter = waiter;
        addChildren(
                new SchoolAdd(this, waiter),
                new SchoolEdit(this),
                new SchoolRemove(this)
        );
    }

    @Override
    public void run(CommandEvent event)
    {

    }
}