package schoolbot.commands.school;

import schoolbot.objects.command.Command;

public class Assignment extends Command
{
      public Assignment()
      {
            super("Adds, Removes, and Edits Professors to the server list", "[add/edit/remove]", 1);
            addCalls("assign", "assignment");
            addChildren(
                    new AssignmentAdd(this),
                    new AssignmentRemove(this),
                    new AssignmentEdit(this)
            );
      }
}
