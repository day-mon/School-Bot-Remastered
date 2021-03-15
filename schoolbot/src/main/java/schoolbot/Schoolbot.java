package schoolbot;

import java.time.LocalDateTime;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import schoolbot.events.MessageRecieve;
import schoolbot.handlers.CommandHandler;
import schoolbot.handlers.ConfigHandler;
import schoolbot.natives.objects.config.ConfigOption;



public class Schoolbot extends ListenerAdapter 
{
	
	private final LocalDateTime botStartTime;
	private final CommandHandler commandHandler;
	private final ConfigHandler configHandler;
	private final EventWaiter eventWaiter;
	private final Logger logger;
	
	private JDA jda;

	public Schoolbot()
	{
		this.logger = LoggerFactory.getLogger(Schoolbot.class);
		this.configHandler = new ConfigHandler(this);
		this.eventWaiter = new EventWaiter();
		this.commandHandler = new CommandHandler(this, eventWaiter);
		this.botStartTime = LocalDateTime.now();	
	}

	public void build() throws LoginException, InterruptedException
	{
		this.jda = JDABuilder.createDefault(configHandler.getString(ConfigOption.TOKEN))
			.addEventListeners(
				new MessageRecieve(this),
				eventWaiter)
			.build()
			.awaitReady();
	}

	public CommandHandler getCommandHandler() 
	{
		return commandHandler;
	}

	public JDA getJda() 
	{
		return jda;
	}

	public LocalDateTime getBotStartTime() 
	{
		return botStartTime;
	}

	public Logger getLogger() 
	{
		return logger;
	}

	public ConfigHandler getConfigHandler() 
	{
		return configHandler;
	}

	public EventWaiter getEventWaiter() 
	{
		return eventWaiter;
	}
	
}