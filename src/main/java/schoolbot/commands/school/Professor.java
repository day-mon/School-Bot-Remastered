package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

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

      }
}
