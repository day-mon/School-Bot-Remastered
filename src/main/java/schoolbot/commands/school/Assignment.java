package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Embed;

import java.util.List;

public class Assignment extends Command
{
      public Assignment()
      {
            super("Adds, Removes, and Edits Professors to the server list", "[add/edit/remove]", 0);
            addCalls("assign", "assignment");
            addChildren(
                    new AssignmentAdd(this),
                    new AssignmentRemove(this),
                    new AssignmentEdit(this)
            );
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Embed.information(event, """
                    I believe you called this command on accident.. That's okay let me help you!
                    You want to ** add ** a assignment? Use ** -assignment add **
                    You want to ** edit ** a assignment? Use ** -assignment edit **
                    or do you want to ** remove ** a assignment? Use ** -assignment remove **
                                    
                    I hope this helps!
                    """);
      }
}
