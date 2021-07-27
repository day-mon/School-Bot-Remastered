package schoolbot.commands.school;

import schoolbot.objects.command.Command;

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
}
