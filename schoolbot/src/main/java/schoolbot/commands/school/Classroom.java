package schoolbot.commands.school;

import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Embed;

public class Classroom extends Command
{

    public Classroom()
    {
        super("Adds, Edits, and Removes Professors", "[add/remove/edit]", 0);
        addCalls("class", "classroom");
        addChildren(
                new ClassroomAdd(this),
                new ClassroomEdit(this),
                new ClassroomRemove(this)
        );
    }

    /**
     * What the command will do on call.
     *
     * @param event Arguments sent to the command.
     */
    @Override
    public void run(CommandEvent event)
    {
        Embed.information(event, """
                I believe you called this command on accident.. That's okay let me help you!
                You want to ** add ** a class? Use ** -class add **
                You want to ** edit ** a class? Use ** -class edit **
                or do you want to ** remove ** a class? Use ** -class remove **
                                
                I hope this helps!
                """);
    }
}
