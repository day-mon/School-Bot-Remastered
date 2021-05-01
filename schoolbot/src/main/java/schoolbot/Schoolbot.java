package schoolbot;

import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Paginator;
import com.github.ygimenez.model.PaginatorBuilder;
import com.github.ygimenez.type.Emote;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.handlers.CommandHandler;
import schoolbot.handlers.ConfigHandler;
import schoolbot.handlers.DatabaseHandler;
import schoolbot.handlers.MessageHandler;
import schoolbot.listener.MainListener;
import schoolbot.natives.objects.config.ConfigOption;
import schoolbot.natives.objects.info.BotInfo;
import schoolbot.natives.objects.info.SystemInfo;
import schoolbot.natives.objects.misc.Emoji;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.time.Duration;
import java.time.LocalDateTime;


public class Schoolbot extends ListenerAdapter
{

	private final LocalDateTime botStartTime;
	private final CommandHandler commandHandler;
	private final ConfigHandler configHandler;
	private final EventWaiter eventWaiter;
	private final DatabaseHandler databaseHandler;
	private final MessageHandler messageHandler;
	private final Logger logger;


	private Paginator paginator;
	private JDA jda;

	public Schoolbot()
	{
		this.logger = LoggerFactory.getLogger(Schoolbot.class);
		this.messageHandler = new MessageHandler(this);
		this.eventWaiter = new EventWaiter();
		this.configHandler = new ConfigHandler(this);
		this.commandHandler = new CommandHandler(this, eventWaiter);
		this.databaseHandler = new DatabaseHandler(this);
		this.botStartTime = LocalDateTime.now();
	}

	public void build() throws LoginException, InterruptedException
	{
		this.jda = JDABuilder.createDefault
				(
						configHandler.getString(ConfigOption.TOKEN),
						GatewayIntent.GUILD_MEMBERS,
						GatewayIntent.GUILD_EMOJIS,

						GatewayIntent.DIRECT_MESSAGES,
						GatewayIntent.DIRECT_MESSAGE_REACTIONS,

						GatewayIntent.GUILD_MESSAGES,
						GatewayIntent.GUILD_MESSAGE_REACTIONS,
						GatewayIntent.GUILD_VOICE_STATES

				)
                .addEventListeners(
                        this,
                        new MainListener(this),
                        eventWaiter)
                .setActivity(Activity.playing("building..."))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();


	}

	@Override
	public void onReady(@Nonnull ReadyEvent event)
	{
		event.getJDA().getPresence().setPresence(OnlineStatus.ONLINE, Activity.competing("Weierman's Lab Speed Run"));
		getLogger().info("Account:           " + event.getJDA().getSelfUser());
		getLogger().info("Java Version:      " + SystemInfo.getJavaVersion());
		getLogger().info("JDA Version:       " + JDAInfo.VERSION);
		getLogger().info("Schoolbot Version: " + BotInfo.getSchoolbotVersion());
		getLogger().info("Operating System:  " + SystemInfo.getOperatingSystem());
		getLogger().info("Github Repo:       " + BotInfo.getGithubRepo());
		getLogger().info("Startup Time:      " + Duration.between(getBotStartTime(), LocalDateTime.now()).toMillisPart() + "ms");

		try
		{
			this.paginator = PaginatorBuilder.createPaginator()
					.setEmote(Emote.NEXT, Emoji.NEXT.getUnicode())
					.setEmote(Emote.PREVIOUS, Emoji.PREVIOUS.getUnicode())
					.setHandler(getJda())
					.shouldRemoveOnReact(false)
					.build();

			Pages.activate(paginator);
		}
		catch (InvalidHandlerException e)
		{
			e.getCause();
		}


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

	public DatabaseHandler getDatabaseHandler()
	{
		return databaseHandler;
	}

	public MessageHandler getMessageHandler()
	{
		return messageHandler;
	}

	public Paginator getPaginator()
	{
		return paginator;
	}

}