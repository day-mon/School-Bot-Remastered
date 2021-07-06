package schoolbot.commands.school;

import schoolbot.objects.command.Command;

public class Classroom extends Command
{

      public Classroom()
      {
            super("Adds, Edits, and Removes Professors", "[add/remove/edit]", 1);
            addCalls("class", "classroom");
            addChildren(
                    new ClassroomRemove(this),
                    new ClassroomAdd(this),
                    new ClassroomEdit(this)
            );
      }
}
