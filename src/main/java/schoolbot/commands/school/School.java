package schoolbot.commands.school;

import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Embed;

import java.util.List;

public class School extends Command
{
      public School()
      {
            super("Adds, Removes, and Edits schools", "[add/edit/remove]", 0);
            addCalls("school", "university");

            addChildren(
                    new SchoolAdd(this),
                    new SchoolEdit(this),
                    new SchoolRemove(this)
            );
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            Embed.information(event, """
                    I believe you called this command on accident.. That's okay let me help you!
                    You want to ** add ** a school? Use ** -school add **
                    You want to ** edit ** a school? Use ** -school edit **
                    or do you want to ** remove ** a school? Use ** -school remove **
                                    
                    I hope this helps!
                    """);
      }
}