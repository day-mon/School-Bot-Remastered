package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class SchoolRemove extends Command
{
    public SchoolRemove(Command parent)
    {
        super(parent, "Removes a school given the name", "[school name]", 1);
    }

    @Override
    public void run(CommandEvent event)
    {

        // event.getSchoolbot().getDatabaseHandler().removeFromTable("schools", "school_name = '" + event.getArgs().get(0) + "'");
        event.getChannel().sendMessage("School: " + event.getArgs().get(0) + " has been deleted").queue();
    }
}
