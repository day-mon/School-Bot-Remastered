package schoolbot.commands.admin;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.Embed;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
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
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();


        if (argCount == 1)
        {
            if (message.getMentionedMembers().size() > 0)
            {
                Member member = message.getMentionedMembers().get(0);
                channel.getIterableHistory()
                        .takeAsync(100)
                        .thenApplyAsync(messages ->
                        {
                            List<Message> channelMessages = messages.stream()
                                    .filter(deleteAbleMessages -> Objects.equals(deleteAbleMessages.getMember(), member))
                                    .filter(deleteAbleMessages -> deleteAbleMessages.getTimeCreated().isAfter(OffsetDateTime.now().minus(24, ChronoUnit.HOURS)))
                                    .limit(25)
                                    .collect(Collectors.toList());
                            channel.purgeMessages(channelMessages);
                            return channelMessages.size();
                        }).whenCompleteAsync((channelMessageSize, throwable) ->
                        channel.sendMessage(
                                "Successfully purged `" + channelMessageSize + "` messages."
                        ).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));
            }
        }
        else if (argCount == 2)
        {

            if (!Checks.isNumber(event.getArgs().get(1)))
            {
                return;

            }

            channel.sendMessage("" + message.getMentionedMembers().size()).queue();

            if (message.getMentionedMembers().size() <= 0)
            {
                return;
            }

            int messagesToRemove = Integer.parseInt(event.getArgs().get(1));


            if (messagesToRemove < 100)
            {

            }

            Member member = message.getMentionedMembers().get(0);
            channel.getIterableHistory()
                    .takeAsync(100)
                    .thenApplyAsync(messages ->
                    {

                        List<Message> channelMessages = messages.stream()
                                .filter(deleteAbleMessages -> Objects.equals(deleteAbleMessages.getMember(), member))
                                .filter(deleteAbleMessages -> deleteAbleMessages.getTimeCreated().isAfter(OffsetDateTime.now().minus(24, ChronoUnit.HOURS)))
                                .limit(messagesToRemove)
                                .collect(Collectors.toList());
                        channel.purgeMessages(channelMessages);
                        return channelMessages.size();
                    }).whenCompleteAsync((channelMessageSize, throwable) ->
                    channel.sendMessage(
                            "Successfully purged `" + channelMessageSize + "` messages."
                    ).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));

        }


        }
}
