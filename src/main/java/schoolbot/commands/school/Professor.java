package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Embed;

import java.util.List;

public class Professor extends Command
{
      public Professor()
      {
            super("Adds, Edits, and Removes Professors", "[add/remove/edit]", 1);
            addCalls("professor", "prof");
            addChildren(
                    new ProfessorAdd(this),
                    new ProfessorEdit(this),
                    new ProfessorRemove(this)
            );
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Embed.information(event, """
                    I believe you called this command on accident.. That's okay let me help you!
                    You want to ** add ** a class? Use ** professor add **
                    You want to ** edit ** a class? Use ** professor edit **
                    or do you want to ** remove ** a class? Use ** professor remove **
                                    
                    I hope this helps!
                    """);
      }
}
