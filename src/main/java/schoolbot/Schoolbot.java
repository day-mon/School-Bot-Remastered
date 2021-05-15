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
import schoolbot.handlers.*;
import schoolbot.listener.MainListener;
import schoolbot.objects.config.ConfigOption;
import schoolbot.objects.info.BotInfo;
import schoolbot.objects.info.SystemInfo;
import schoolbot.objects.misc.Emoji;

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


      private Paginator paginator;
      private JDA jda;

      public Schoolbot()
      {
            this.LOGGER = LoggerFactory.getLogger(Schoolbot.class);
            this.messageHandler = new MessageHandler(this);
            this.eventWaiter = new EventWaiter();
            this.configHandler = new ConfigHandler(this);
            this.wrapperHandler = new WrapperHandler(this);
            this.commandHandler = new CommandHandler(this);
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
            getJda().getPresence().setPresence(OnlineStatus.ONLINE, Activity.streaming("Prefix: " + SchoolbotConstants.DEFAULT_PREFIX, "https://www.youtube.com/watch?v=Lju6h-C37hE"));
            getLogger().info("Account:           " + event.getJDA().getSelfUser());
            getLogger().info("Java Version:      " + SystemInfo.getJavaVersion());
            getLogger().info("JDA Version:       " + JDAInfo.VERSION);
            getLogger().info("Schoolbot Version: " + BotInfo.getSchoolbotVersion());
            getLogger().info("Operating System:  " + SystemInfo.getOperatingSystem());
            getLogger().info("Github Repo:       " + BotInfo.getGithubRepo());
            getLogger().info("Startup Time:      " + Duration.between(getBotStartTime(), LocalDateTime.now()).toMillisPart() + " ms");


            try
            {
                  this.paginator = PaginatorBuilder.createPaginator()
                          .setEmote(Emote.NEXT, Emoji.ARROW_RIGHT.getUnicode())
                          .setEmote(Emote.PREVIOUS, Emoji.ARROW_LEFT.getUnicode())
                          .setEmote(Emote.GOTO_FIRST, Emoji.TRACK_PREVIOUS.getUnicode())
                          .setEmote(Emote.GOTO_LAST, Emoji.TRACK_NEXT.getUnicode())
                          .setHandler(getJda())
                          .shouldRemoveOnReact(false)
                          .build();

                  Pages.activate(paginator);
            }
            catch (InvalidHandlerException e)
            {
                  getLogger().error("Paginator Error... Exiting because half of the bot uses paginator", e);
                  System.exit(1);
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

      public Paginator getPaginator()
      {
            return paginator;
      }

      public WrapperHandler getWrapperHandler()
      {
            return wrapperHandler;
      }
}