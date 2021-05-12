package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.List;

public class ProfessorEdit extends Command
{
    public ProfessorEdit(Command parent)
    {
        super(parent, "Edits a professor to the server list", "[professor name] [attribute to edit] [edit]", 1);
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void run(@NotNull CommandEvent event, @NotNull List<String> args)
    {

    }
}
