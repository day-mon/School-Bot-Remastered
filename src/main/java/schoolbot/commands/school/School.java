package schoolbot.commands.school;

import schoolbot.objects.command.Command;

public class School extends Command
{
      public School()
      {
            super("Adds, Removes, and Edits schools", "[add/edit/remove]", 1);
            addCalls("school", "university");
            addChildren(
                    new SchoolAdd(this),
                    new SchoolEdit(this),
                    new SchoolRemove(this)
            );
      }
}