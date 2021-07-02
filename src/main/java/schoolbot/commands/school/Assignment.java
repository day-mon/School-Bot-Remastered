package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.EmbedUtils;

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
            EmbedUtils.information(event, """
                    I believe you called this command on accident.. That's okay let me help you!
                    You want to ** add ** a assignment? Use ** assignment add **
                    You want to ** edit ** a assignment? Use ** assignment edit **
                    or do you want to ** remove ** a assignment? Use ** assignment remove **
                                    
                    I hope this helps!
                    """);
      }
}
