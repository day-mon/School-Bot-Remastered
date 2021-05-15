package schoolbot.commands.school.pitt;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;

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
