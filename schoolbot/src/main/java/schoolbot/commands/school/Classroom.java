package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class Classroom extends Command
{

    public Classroom()
    {
        super("Adds, Edits, and Removes Professors", "[add/remove/edit]", 1);
        addCalls("class", "classroom");
        addChildren(
                new ClassroomAdd(this),
                new ClassroomEdit(this),
                new ClassroomRemove(this)
        );
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
