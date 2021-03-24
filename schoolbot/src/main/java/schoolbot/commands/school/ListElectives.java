package schoolbot.commands.school;

import net.dv8tion.jda.api.EmbedBuilder;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import javax.swing.plaf.ColorUIResource;

public class ListElectives extends Command
{

    public ListElectives()
    {
        super("", "", 0);
        addCalls("woks", "listwoks", "electives", "listelectives", "elist");
    }


    @Override
    public void run(CommandEvent event)
    {
        event.sendMessage(new EmbedBuilder()
                                    .setTitle("World of Knowledge (WOK's)", "https://www.johnstown.pitt.edu/academics/office-registrar/academic-curriculum-requirements")
                                    .addField("Aesthetic and Creative Expression", "https://www.johnstown.pitt.edu/sites/default/files/WOK%20A%26CE%2020-21_0.pdf", false)
                                    .addField("Global History and Culture", "https://www.johnstown.pitt.edu/sites/default/files/WOK%20GH%26C%2020-21.pdf", false)
                                    .addField("Science and Nature", "https://www.johnstown.pitt.edu/sites/default/files/WOK%20S%26N%2020-21.pdf", false)
                                    .addField("Society and Civics", "https://www.johnstown.pitt.edu/sites/default/files/WOK%20S%26C%2020-21_0.pdf", false), ColorUIResource.CYAN);
    }
}
