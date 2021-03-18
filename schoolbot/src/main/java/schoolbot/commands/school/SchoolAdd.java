package schoolbot.commands.school;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.Response;
import schoolbot.SchoolbotConstants;
import schoolbot.events.ResponseStateMachine;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.Embed;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class SchoolAdd extends Command
{
    private final EventWaiter waiter;
    public SchoolAdd(Command parent, EventWaiter waiter)
    {
        super(parent,"Adds a school to the server", "[school name] [school email suffix] [school reference]", 0);
        this.waiter = waiter;
    }


    @Override
    public void run(CommandEvent event)
    {
        MessageChannel channel = event.getChannel();
        User user = event.getUser();
    /*
        ResponseStateMachine r = new ResponseStateMachine(event.getUser(), event.getChannel());
        channel.sendMessage("Hello " + user.getName() + " You would like to add a school? What is this name?: ").queue();
        r.onMessageReceived(event.getEvent());
        channel.sendMessage("Now that we have your school name, What is your email suffix (i.e @pitt.edu): ").queue();
        r.onMessageReceived(event.getEvent());

        /*
        MessageChannel channel = event.getChannel();
        User user = event.getUser();
        channel.sendMessage("Hello " + event.getMember().getNickname() + " You would like to add a school? What is this name?: ")
                .queue(reply ->
                {

                    this.waiter.waitForEvent(MessageReceivedEvent.class,
                            messageReceivedEvent ->
                            {
                                if (!Objects.equals(messageReceivedEvent.getAuthor(), event.getUser()))
                                {
                                    return false;
                                }

                                if (!Objects.equals(messageReceivedEvent.getChannel(), channel))
                                {
                                    return false;
                                }

                                return true;
                            },
                            action ->
                            {
                                if (!action.getMessage().getContentRaw().replaceAll("\\s+", "").chars().allMatch(Character::isAlphabetic))
                                {
                                    Embed.error(event, "School name cannot contain non-alphabetic characters!\n SchoolAdd has been aborted!");
                                    return;
                                }
                                channel.sendMessage("Your school name will be: " + action.getMessage().getContentRaw()).queue();

                                channel.sendMessage("Now that we have your school name, What is your email suffix (i.e @pitt.edu): ").queue(v -> {
                                    this.waiter.waitForEvent(MessageReceivedEvent.class,
                                            messageReceivedEvent ->
                                            {
                                                if (!Objects.equals(messageReceivedEvent.getAuthor(), event.getUser()))
                                                {
                                                    return false;
                                                }

                                                if (!Objects.equals(messageReceivedEvent.getChannel(), channel))
                                                {
                                                    return false;
                                                }

                                                return true;                                            },
                                            action1 ->
                                            {
                                                if(!action1.getMessage().getContentRaw().contains("edu"))
                                                {
                                                    Embed.error(event, "This is not a valid email!");
                                                    return;
                                                }
                                                channel.sendMessage("Your school email will be: " + action1.getMessage().getContentRaw()).queue();

                                                channel.sendMessage(new EmbedBuilder()
                                                        .setTitle("New School Created!")
                                                        .addField("School name", action.getMessage().getContentRaw(), false)
                                                        .addField("School Email", action1.getMessage().getContentRaw(), false)
                                                        .setFooter("Created by " + user.getName() ,user.getAvatarUrl())
                                                        .setTimestamp(Instant.now())
                                                        .setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR)
                                                        .build()).queue();

                                            }




                                            );
                                }); //send message here?
                            });
                });

    }

         */
    }



}
