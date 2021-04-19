package schoolbot.commands.school;

import okhttp3.OkHttpClient;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class AssignmentAdd extends Command
{
    // 13997~cAmxhqw2BCwj6SK2vF5Fdeu8lpa1pWIIrz2c0316oK0bbYKGpdZE48l89x3BzSBF

    private final OkHttpClient client;

    /**
     * @param parent
     */
    public AssignmentAdd(Command parent)
    {
        super(parent, " ", " ", 1);
        client = new OkHttpClient();
    }

    /**
     * @param event Arguments sent to the command.
     */
    @Override
    public void run(CommandEvent event)
    {

    }
}
