package schoolbot.commands.school;

import schoolbot.objects.command.Command;

public class Reminder extends Command
{

      public Reminder()
      {
            super("description", "syntax", 0);
            addCalls("reminders", "reminder");
            addChildren(
                    new ReminderView(this)
                   // new ReminderRemove(this)
            );
      }


}
