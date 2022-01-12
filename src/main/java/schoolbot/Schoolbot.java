package schoolbot;

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
import schoolbot.handlers.*;
import schoolbot.listeners.*;
import schoolbot.objects.info.SystemInfo;

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
      private final WrapperHandler wrapperHandler;
      private final Logger LOGGER;

      private ScheduleHandler scheduleHandler;
      private JDA jda;

      public Schoolbot()
      {
            this.botStartTime = LocalDateTime.now();
            this.LOGGER = LoggerFactory.getLogger(Schoolbot.class);
            this.messageHandler = new MessageHandler(this);
            this.eventWaiter = new EventWaiter();
            this.configHandler = new ConfigHandler(this);
            this.wrapperHandler = new WrapperHandler(this);
            this.commandHandler = new CommandHandler(this);
            this.databaseHandler = new DatabaseHandler(this);
      }

      public void build() throws LoginException
      {
            this.jda = JDABuilder.createDefault(configHandler.getConfig().getToken())
                    .enableIntents(
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGE_REACTIONS,

                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_VOICE_STATES
                    )
                    .addEventListeners(
                            this,
                            new MessageListener(this),
                            new ChannelListener(this),
                            new RoleListener(this),
                            new SelfLeaveListener(this),
                            new SelfJoinListener(this),
                            eventWaiter)
                    .setActivity(Activity.playing("building..."))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .build();
      }

      @Override
      public void onReady(@Nonnull ReadyEvent event)
      {

            this.scheduleHandler = new ScheduleHandler(this);

            LOGGER.info("Account:             " + jda.getSelfUser());
            LOGGER.info("Java Version:        " + SystemInfo.getJavaVersion());
            LOGGER.info("JDA Version:         " + JDAInfo.VERSION);
            LOGGER.info("Schoolbot Version:   " + Constants.VERSION);
            LOGGER.info("Operating System:    " + SystemInfo.getOperatingSystem());
            LOGGER.info("Github Repo:         " + "https://github.com/tykoooo/School-Bot-Remastered");
            LOGGER.info("Startup Time:        " + Duration.between(botStartTime, LocalDateTime.now()).toMillis() + " ms");
            LOGGER.info("Developer User Ids:  " + configHandler.getConfig().getDeveloperIds());
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
            return LOGGER;
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

      public WrapperHandler getWrapperHandler()
      {
            return wrapperHandler;
      }
}