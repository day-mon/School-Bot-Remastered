package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;

import java.util.List;

public class ClassroomRemove extends Command
{

      public ClassroomRemove(Command parent)
      {
            super("Removes a class from a school", "", 0);
            addPermissions(Permission.ADMINISTRATOR);
            addFlags(CommandFlag.DATABASE);

      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

      }
}
