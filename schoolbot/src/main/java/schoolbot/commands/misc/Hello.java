package schoolbot.commands.misc;

import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class Hello extends Command 
{
    private final EventWaiter waiter;

    public Hello(EventWaiter waiter)
    {
        super("s", "s", 0);
        this.waiter = waiter;
        addCalls("hello");
    }
    
    @Override
    public void run(CommandEvent event)
    {
        // ask what the user's name is
        event.sendMessage("Hello. What is your name?");
        // wait for a response
        System.out.println(waiter.toString());
        waiter.waitForEvent(MessageReceivedEvent.class, 
                // make sure it's by the same user, and in the same channel, and for safety, a different message
                e -> e.getAuthor().equals(event.getUser()) 
                        && e.getChannel().equals(event.getChannel()) 
                        && !e.getMessage().equals(event.getMessage()), 
                // respond, inserting the name they listed into the response
                e -> event.sendMessage("Hello, `"+e.getMessage().getContentRaw()+"`! I'm `"+e.getJDA().getSelfUser().getName()+"`!"),
                // if the user takes more than a minute, time out
                1, TimeUnit.MINUTES, () -> event.sendMessage("Sorry, you took too long."));
    }
}
