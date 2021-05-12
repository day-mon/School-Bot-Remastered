package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.List;

public class ClassroomEdit extends Command
{

    public ClassroomEdit(Command parent)
    {
        super("", "", 0);
    }

    @Override
    public void run(@NotNull CommandEvent event, @NotNull List<String> args)
    {

    }
}
