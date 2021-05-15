package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;

import java.util.List;

public class SchoolEdit extends Command
{
      public SchoolEdit(Command parent)
      {
            super(parent, "Edits a school", "[none]", 0);
            addPermissions(Permission.ADMINISTRATOR);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

      }
}
