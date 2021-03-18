package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class SchoolEdit extends Command
{
    public SchoolEdit(Command parent)
    {
        super(parent, "Edits a school given the name", "[school name] [attribute to edit] [edit]", 0);
        addPermissions(Permission.ADMINISTRATOR);
    }

    @Override
    public void run(CommandEvent event)
    {
        event.getChannel().sendMessage("test works").queue();
    }
}
