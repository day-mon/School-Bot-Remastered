package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Embed;

import java.util.List;

public class Classroom extends Command
{

      public Classroom()
      {
            super("Adds, Edits, and Removes Professors", "[add/remove/edit]", 0);
            addCalls("class", "classroom");
            addChildren(
                    new ClassroomRemove(this),
                    new ClassroomAdd(this),
                    new ClassroomEdit(this)
            );
      }

      /**
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Embed.information(event, """
                    I believe you called this command on accident.. That's okay let me help you!
                    You want to ** add ** a class? Use ** class add **
                    You want to ** edit ** a class? Use ** class edit **
                    or do you want to ** remove ** a class? Use ** class remove **
                                    
                    I hope this helps!
                    """);
      }
}
