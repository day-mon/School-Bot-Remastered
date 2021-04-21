package schoolbot.commands.school.pitt;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;

public class Calender extends Command
{

    public Calender()
    {
        super("", " ", 1);
        addCalls("cal", "calender");
        addFlags(CommandFlag.INTERNET);

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
