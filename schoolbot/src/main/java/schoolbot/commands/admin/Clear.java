package schoolbot.commands.admin;

import java.util.stream.Collectors;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.misc.Emoji;
import schoolbot.natives.util.Embed;

public class Clear extends Command 
{

    private final EventWaiter waiter;

    public Clear(EventWaiter waiter)
    {
        super("Clears a certain amount of messages inputted by the user", "[optional: number of messages]", 0);
        addCalls("clear", "destroy", "clean");
        this.waiter = waiter;
    }

    @Override
    public void run(CommandEvent event) 
    {
        if (event.getArgs().isEmpty())
        {
              event.getChannel().sendMessage(new EmbedBuilder()
                                .setDescription("You're about to delete 100 messages. Are you sure you wanna do this?").build()).queue(message -> {
                                    message.addReaction(Emoji.WHITE_CHECK_MARK.getUnicode()).queue();
                                    message.addReaction(Emoji.CROSS_MARK.getUnicode()).queue();
                                });
              awaitReaction(event);
            

        }
    }

    private void awaitReaction(CommandEvent event)
    {
        waiter.waitForEvent(GuildMessageReactionAddEvent.class, 
                             e -> !e.getMember().getUser().isBot(),
                             e -> {
                                if (e.getReactionEmote().getAsCodepoints().equalsIgnoreCase(Emoji.CROSS_MARK.getUnicode()))
                                {
                                    Embed.error(event, "Aborting...");
                                }
                                else if (e.getReactionEmote().getAsCodepoints().equals(Emoji.WHITE_CHECK_MARK.getUnicode()))
                                {
                                    event.getChannel().getIterableHistory().takeAsync(100).thenAccept(messages -> {
                                        event.getChannel().purgeMessages(messages);
                                    });
                                }
                            });
    }
    
}
