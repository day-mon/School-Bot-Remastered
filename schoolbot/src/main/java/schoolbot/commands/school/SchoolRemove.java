package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class SchoolRemove extends Command
{
    public SchoolRemove(Command parent)
    {
        super(parent, "Edits a school given the name", "[school name] [ed", 0);
    }

    @Override
    public void run(CommandEvent event)
    {

    }
}
