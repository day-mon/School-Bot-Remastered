package schoolbot.commands;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class StateMachineTest<T extends GenericEvent> implements EventListener
{
    private Predicate<T> check;
    private BiFunction<Integer, T, Integer> biFunction;
    private Class<T> clazz;
    private int state = 0;

    public StateMachineTest(Predicate<T> check, BiFunction<Integer, T, Integer> biFunction, Class<T> clazz)
    {
        this.clazz = clazz;
        this.check = check;
        this.biFunction = biFunction;
    }


    public void onEvent(GenericEvent event)
    {
        if (clazz.isInstance(event))
        {
            T castedEvent = clazz.cast(event);
            if (check.test(castedEvent))
            {
                state = biFunction.apply(state, castedEvent);
            }
        }
    }

   
   

   
   
   
   
   
   
   
   
   
   
   
}
