package schoolbot.commands.school.pitt;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;

import java.util.List;

public class Calender extends Command
{

    public Calender()
    {
        super("", " ", 1);
        addCalls("cal", "calender");
        addFlags(CommandFlag.INTERNET);

    }


    @Override
    public void run(@NotNull CommandEvent event, @NotNull List<String> args)
    {

    }
}
