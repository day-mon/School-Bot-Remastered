package schoolbot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.openqa.selenium.devtools.v86.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import schoolbot.events.MessageRecieve;
import schoolbot.handlers.CommandHandler;
import schoolbot.handlers.ConfigHandler;
import schoolbot.handlers.DatabaseHandler;
import schoolbot.natives.objects.config.ConfigOption;
import schoolbot.natives.objects.info.BotInfo;


public class Schoolbot extends ListenerAdapter
{
	
	private final LocalDateTime botStartTime;
	private final CommandHandler commandHandler;
	private final ConfigHandler configHandler;
	private final EventWaiter eventWaiter;
	private final DatabaseHandler databaseHandler;
	private final Logger logger;
	
	private JDA jda;

	public Schoolbot()
	{
		this.logger = LoggerFactory.getLogger(Schoolbot.class);
		this.eventWaiter = new EventWaiter();
		this.configHandler = new ConfigHandler(this);
		this.commandHandler = new CommandHandler(this, eventWaiter);
		this.databaseHandler = new DatabaseHandler(this);
		this.botStartTime = LocalDateTime.now();	
	}

	public void build() throws LoginException, InterruptedException
	{
		this.jda = JDABuilder.createDefault(configHandler.getString(ConfigOption.TOKEN))
			.addEventListeners(
					this,
				new MessageRecieve(this),
				eventWaiter)
				.setActivity(Activity.playing("building..."))
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.build();
	}

	@Override
	public void onReady(@Nonnull ReadyEvent event)
	{
		event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.competing("Weierman's Lab Speed Run"));

		getLogger().info("Account:           "  + event.getJDA().getSelfUser());
		getLogger().info("Java Version:      "  + BotInfo.getJavaVersion());
		getLogger().info("JDA Version:       "  + JDAInfo.VERSION);
		getLogger().info("Schoolbot Version: "  + BotInfo.getSchoolbotVersion());
		getLogger().info("Operating System:  "  + System.getProperty("os.name"));
		getLogger().info("Github Repo:       "  + BotInfo.getGithubRepo());
		getLogger().info("Startup Time:      "  + Duration.between(getBotStartTime(), LocalDateTime.now()).toMillisPart()+ "ms");


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

	public DatabaseHandler getDatabaseHandler() {
		return databaseHandler;
	}
}