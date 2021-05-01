package schoolbot.commands.school;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.util.Checks;

public class AssignmentAdd extends Command
{

    /**
     * @param parent
     */
    public AssignmentAdd(Command parent)
    {
        super(parent, " ", " ", 1);
        addFlags(CommandFlag.DATABASE);

    }

    /**
     * @param event Arguments sent to the command.
     */
    @Override
    public void run(CommandEvent event)
    {
        String arg0 = event.getArgs().get(0);

        if (Checks.isNumber(arg0))
        {
            // TODO: Start this command by 05/02/2021
        }


    }

    public static class AssignmentAddStateMachine extends ListenerAdapter
    {
        private final long channelID, authorID;
        private int state = 1;
        private Schoolbot schoolbot;
        private Assignment assignment;


        public AssignmentAddStateMachine(Schoolbot schoolbot, MessageChannel channel, User author)
        {
            this.channelID = channel.getIdLong();
            this.authorID = author.getIdLong();
            this.schoolbot = schoolbot;
        }

        @Override
        public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
        {
            if (event.getAuthor().isBot()) return;
            if (event.getChannel().getIdLong() != channelID) return;
            if (event.getAuthor().getIdLong() != authorID) return;

            MessageChannel channel = event.getChannel();
            String content = event.getMessage().getContentRaw();

            switch (state)
            {
                case 1 -> {

                }

            }
        }
    }
}
