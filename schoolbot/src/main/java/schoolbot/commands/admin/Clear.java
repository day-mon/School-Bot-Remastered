// Package
package schoolbot.commands.admin;

// Imports

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.Permission;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.Embed;

import java.awt.*;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.List;

public class Clear extends Command
{
    private final EventWaiter waiter;

    public Clear(EventWaiter waiter)
    {
        super("Clears messages in the text channel that the command was executed in", "[optional: number]", 0);
        addCalls("purge", "clean", "clear");
        //  addPermissions(Permission.ADMINISTRATOR);
        // addSelfPermissions(Permission.MESSAGE_MANAGE);
        addChildren(
                new ClearUser(this, waiter)
        );
        this.waiter = waiter;

    }

    public void run(CommandEvent event)
    {
        if (event.getArgs().isEmpty())
        {
            MessageChannel channel = event.getChannel();
            Message message = event.getMessage();
            Member member = event.getMember();
            User author = event.getUser();


            if (!message.isFromGuild())
            {
                author.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(
                        "This command must be used in a guild."
                )).queue();

                return;
            }

            // The warning here is annoying, suppress if you must; I don't even think it's possible for Member to return null here.

            channel.sendMessage(
                    "You are about to delete 100 messages, click the checkmark to continue, click the X to cancel."
            ).queue(prompt ->
            {
                prompt.addReaction("\u2705").queue(); // Checkmark.
                prompt.addReaction("\u274E").queue(); // X-Mark.

                this.waiter.waitForEvent(MessageReactionAddEvent.class, reactionEvent -> reactionEvent.getMessageIdLong() == prompt.getIdLong()
                        && Objects.equals(reactionEvent.getUser(), author) || Objects.equals(reactionEvent.getUser().getId(), SchoolbotConstants.GENIUS_OWNER_ID), reactionEvent ->
                {

                    switch (reactionEvent.getReaction().getReactionEmote().getName())
                    {
                        case "\u2705":
                        {
                            prompt.getChannel().getIterableHistory()
                                    .takeAsync(100)
                                    .thenApplyAsync(channelMessages ->
                                    {
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
                        case "\u274E":
                        {
                            channel.sendMessage("Operation was successfully cancelled.").queue();

                            break;
                        }
                        default:
                        {
                            channel.sendMessage("You did not select one of the available options.").queue();

                            break;
                        }
                    }
                }, 15, TimeUnit.SECONDS, () ->
                {
                    prompt.delete().queue();

                    channel.sendMessage("You did not respond in time.").queue();
                });
            });

        }
        else
        {
            var numcheck = event.getArgs().get(0);
            if (!Checks.isNumber(numcheck))
            {
                Embed.error(event, "This is not a number! " +
                        "\nUsage: " + this.getUsageExample());
                return;
            }
            int messagesToRemove = Integer.parseInt(numcheck);
            event.getChannel().getIterableHistory()
                    .takeAsync(messagesToRemove)
                    .thenApplyAsync(channelMessages ->
                    {
                        List<Message> filteredDeletedMessages = channelMessages.stream()
                                .filter(messages -> messages.getTimeCreated().isBefore(
                                        OffsetDateTime.now().plus(2, ChronoUnit.WEEKS)
                                )).collect(Collectors.toList());
                        event.getChannel().purgeMessages(filteredDeletedMessages);
                        return filteredDeletedMessages.size();
                    }).whenCompleteAsync((messagesTotal, throwable) -> event.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setDescription("Successfully purged `" + messagesTotal + "` messages.")
                            .setColor(Color.GREEN)
                            .build()).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));
        }
    }
}
