package schoolbot.commands.misc;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class Ask extends Command
{
    public Ask()
    {
        super("Command to call when someone ask a non-valid question", "[none]", 0);
        addCalls("ask");
    }

    @Override
    public void run(CommandEvent event) 
    {
        event.sendMessage("Refer to: https://dontasktoask.com/");      
    }   
}
