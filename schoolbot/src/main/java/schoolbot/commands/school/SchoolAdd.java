package schoolbot.commands.school;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

import java.util.EventListener;
import java.util.function.Predicate;

public class SchoolAdd extends Command
{
    private final EventWaiter waiter;

    public SchoolAdd(Command parent, EventWaiter waiter)
    {
        super(parent, "Adds a school to the server", "[school name] [school email suffix] [school reference]", 0);
        this.waiter = waiter;
    }


    @Override
    public void run(CommandEvent event)
    {

        MessageChannel channel = event.getChannel();
        User user = event.getUser();


    }

    public interface State<T, S extends State<T, S>>
    {
        default boolean isEnd()
        {
            return false;
        }

        S apply(T input);
    }

    public enum SchoolAddState implements State<MessageReactionAddEvent, SchoolAddState>
    {
        SCHOOL_NAME, SCHOOL_EMAIL_STATE, END;

        @Override
        public boolean isEnd()
        {
            return this == END;
        }

        public boolean isSchoolName()
        {
            return this == SCHOOL_NAME;
        }

        public boolean isSchoolEmailState()
        {
            return this == SCHOOL_EMAIL_STATE;
        }


        @Override
        public SchoolAddState apply(MessageReactionAddEvent input)
        {
            return END;
        }
    }

    public class StateMachine<T extends GenericEvent, S extends State<T, S>> implements EventListener
    {
        private final Predicate<? super T> check;
        private final Class<T> clazz;
        private S state;

        public StateMachine(Predicate<? super T> check, Class<T> clazz, S init)
        {
            this.check = check;
            this.clazz = clazz;
            this.state = init;
        }


    }

}

