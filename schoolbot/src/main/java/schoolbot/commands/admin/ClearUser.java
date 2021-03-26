package schoolbot.commands.admin;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Embed;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClearUser extends Command
{
    private final EventWaiter waiter;
    public ClearUser(Command parent, EventWaiter waiter)
    {
        super(parent, "", "", 1);
        this.waiter = waiter;
        addPermissions(Permission.ADMINISTRATOR);
        addSelfPermissions(Permission.MESSAGE_MANAGE);
    }

    @Override
    public void run(CommandEvent event)
    {
        int argCount = event.getArgs().size();
        MessageChannel channel = event.getChannel();
        User author = event.getUser();

        if (argCount == 1)
        {
            channel.sendMessage(
                    "You are about to delete 100 messages, click the checkmark to continue, click the X to cancel."
            ).queue(prompt -> {
                prompt.addReaction("\u2705").queue(); // Checkmark.
                prompt.addReaction("\u274E").queue(); // X-Mark.

                this.waiter.waitForEvent(MessageReactionAddEvent.class, reactionEvent -> reactionEvent.getMessageIdLong() == prompt.getIdLong()
                        && Objects.equals(reactionEvent.getUser(), author) || Objects.equals(reactionEvent.getUser().getId(), SchoolbotConstants.GENIUS_OWNER_ID), reactionEvent -> {

                    switch (reactionEvent.getReaction().getReactionEmote().getName()) {
                        case "\u2705": {
                            prompt.getChannel().getIterableHistory()
                                    .takeAsync(100)
                                    .thenApplyAsync(channelMessages -> {
                                        List<Message> deletableMessages = channelMessages.stream()
                                                .filter(messages -> messages.getTimeCreated().isBefore(
                                                        OffsetDateTime.now().plus(2, ChronoUnit.WEEKS)
                                                )).collect(Collectors.toList());

                                        channel.purgeMessages(deletableMessages);

                                        return deletableMessages.size();
                                    }).whenCompleteAsync((messagesTotal, throwable) -> channel.sendMessage(
                                    "Successfully purged `" + messagesTotal + "` messages."
                            ).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));

                            break;
                        }
                        case "\u274E": {
                            channel.sendMessage("Operation was successfully cancelled.").queue();

                            break;
                        }
                        default: {
                            channel.sendMessage("You did not select one of the available options.").queue();

                            break;
                        }
                    }
                }, 15, TimeUnit.SECONDS, () -> {
                    prompt.delete().queue();

                    channel.sendMessage("You did not respond in time.").queue();
                });
            });
        }
        else if (argCount == 2)
        {

        }
        else
        {
            Embed.error(event, "Too many args!");
        }
    }
}
