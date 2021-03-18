package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class ProfessorAdd extends Command
{
    public ProfessorAdd(Command parent)
    {
        super(parent, "Adds a professor to the server list", "[school name] [professor name] [professor email]", 1);
    }

    @Override
    public void run(CommandEvent event)
    {
        event.sendMessage("professor add works");
    }
}
