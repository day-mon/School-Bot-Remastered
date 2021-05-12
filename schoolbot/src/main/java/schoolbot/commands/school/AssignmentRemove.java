package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.List;

public class AssignmentRemove extends Command
{
    /**
     * @param parent
     */
    public AssignmentRemove(Command parent)
    {
        super(parent, " ", " ", 1);

    }


    @Override
    public void run(@NotNull CommandEvent event, @NotNull List<String> args)
    {

    }
}
