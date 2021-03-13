package schoolbot.natives.objects.misc;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Event {
    
    protected String[] triggers;
    protected String name;

    public Event() {

    }

    public Event(String [] triggers) 
    {
        this.triggers = new String[triggers.length];
        for (int i = 0; i <  triggers.length; i++) 
            this.triggers[i] = triggers[i];
        this.name = this.getClass().getSimpleName();
    }
 
    public abstract void run(MessageReceivedEvent event);

    public String[] getTriggers() {
        return this.triggers;
    }

    public String getName() {
        return name;
    }

    public boolean isInTriggers(String test) {
        for (String trigger : triggers) {
            if (test.contains(trigger)) {
                return true;
            }
        }
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }
}
