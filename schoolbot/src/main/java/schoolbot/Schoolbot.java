package schoolbot;

import java.time.LocalDateTime;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.DefaultShardManager;
import net.dv8tion.jda.api.sharding.ShardManager;
import schoolbot.events.MessageRecieve;
import schoolbot.handlers.CommandHandler;
import schoolbot.handlers.ConfigHandler;
import schoolbot.natives.objects.config.ConfigOption;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;



public class Schoolbot extends ListenerAdapter 
{
	private final CommandHandler commandHandler;
	private final ConfigHandler configHandler;

	private final LocalDateTime botStartTime;
	
	private JDA jda;

	public Schoolbot()
	{
		this.commandHandler = new CommandHandler(this);
		this.configHandler = new ConfigHandler();
		this.botStartTime = LocalDateTime.now();
	}

	public void build() throws LoginException, InterruptedException
	{
		this.jda = JDABuilder.createDefault(configHandler.getString(ConfigOption.TOKEN))
			.addEventListeners(
				new MessageRecieve(this))
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
	
}