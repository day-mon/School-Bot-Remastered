package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class Professor extends Command
{
    public Professor()
    {
        super("Adds, Edits, and Removes Professors", "[add/remove/edit]", 1 );
        addCalls("professor", "prof");
        addChildren(
                new ProfessorAdd(this),
                new ProfessorEdit(this),
                new ProfessorRemove(this)
        );
    }

    @Override
    public void run(CommandEvent event)
    {

    }
}
