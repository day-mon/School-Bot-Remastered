package schoolbot.interactions;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.objects.misc.interfaces.Paginator;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ButtonPaginator implements Paginator
{

      private static final Logger LOGGER = LoggerFactory.getLogger(ButtonPaginator.class);

      private final EventWaiter waiter;
      private final TimeUnit timeUnit;
      private final JDA jda;

      private final List<MessageEmbed> embeds;
      private final List<Button> buttons;

      private boolean deleteOnTimeout;

      private long messageId = -1;
      private final long channelId;

      private int page = 0;
      private final int maxPage;
      private final int timeout;


      public ButtonPaginator(Builder builder)
      {
            this.embeds = builder.embeds;
            this.waiter = builder.eventWaiter;
            this.timeout = builder.timeout;
            this.channelId = builder.channelId;
            this.timeUnit = builder.timeUnit;
            this.buttons = builder.emojis;
            this.jda = builder.jda;
            this.deleteOnTimeout = builder.deleteOnTimeOut;
            this.maxPage = embeds.size();
      }

      private void switchPage(ButtonClickEvent event)
      {
            event.deferEdit().queue();
            final var buttonId = event.getButton().getId();
            final var message = event.getMessage();

            if (buttonId == null)
            {
                  LOGGER.error("Oopsies. Button ID is null for some reason");
                  return;
            }

            if (message == null)
            {
                  LOGGER.error("Oopsies. Message is null for some reason");
                  return;
            }

            if (messageId == event.getMessageIdLong() && message.getAuthor().getIdLong() == event.getUser().getIdLong())
                  return;

            switch (buttonId)
            {
                  case "stop" -> message.delete().queue();

                  case "previous" -> {
                        page--;
                        if (page < 0) page = maxPage - 1;
                  }
                  case "next" -> {
                        page++;
                        if (page >= maxPage) page = 0;
                  }

                  case "start" -> page = 0;

                  case "end" -> page = embeds.size() - 1;
            }

            message.editMessageEmbeds(embeds.get(page))
                    .setActionRows(getActionRow())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
            doWait();
      }

      private ActionRow getActionRow()
      {
            return ActionRow.of(page < 1 ? start.asDisabled() : start, page < 1 ? previous.asDisabled() : previous, stop, page + 1 == maxPage ? next.asDisabled() : next, page + 1 == maxPage ? end.asDisabled() : end);
      }


      private void doWait()
      {
            waiter.waitForEvent(ButtonClickEvent.class, event -> messageId == event.getMessageIdLong() && event.getUser().getIdLong() == event.getUser().getIdLong(), this::switchPage, timeout, timeUnit, () ->
            {
                  if (this.deleteOnTimeout)
                  {
                        if (this.messageId == -1)
                        {
                              LOGGER.error("ID not set (this should never happen)", new IllegalArgumentException());
                              return;
                        }

                        MessageChannel channel = getChannel();
                        if (channel == null)
                        {
                              LOGGER.error("Channel does not exist for ID " + this.channelId, new IllegalStateException());
                              return;
                        }

                        channel.deleteMessageById(this.messageId).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                  }
            });
      }


      private void send()
      {
            MessageChannel channel = getChannel();

            if (channel == null)
            {
                  throw new IllegalArgumentException("Channel does not exist for ID " + channelId);
            }

            channel.sendMessageEmbeds(embeds.get(page))
                    .setActionRows(getActionRow())
                    .queue(m -> this.messageId = m.getIdLong());
      }

      private MessageChannel getChannel()
      {
            MessageChannel channel = jda.getTextChannelById(channelId);
            if (channel == null)
            {
                  channel = jda.getPrivateChannelById(channelId);
            }

            return channel;
      }

      @Override
      public void paginate()
      {
            send();
            doWait();
      }

      @Override
      public int getTimeout()
      {
            return timeout;
      }

      @Override
      public TimeUnit getTimeoutUnit()
      {
            return timeUnit;
      }

      @Override
      public int getPage()
      {
            return page;
      }

      public static class Builder
      {
            private List<Button> emojis = List.of(previous, stop, next);
            private List<MessageEmbed> embeds = new ArrayList<>();

            private int timeout = DEFAULT_TIMEOUT;
            private TimeUnit timeUnit = DEFAULT_TIMEOUT_UNIT;
            private boolean deleteOnTimeOut = DEFAULT_DELETE_ON_TIMEOUT;
            private EventWaiter eventWaiter = null;
            private long channelId = -1L;
            private JDA jda = null;


            public Builder setEmojis(List<Button> emojis)
            {
                  this.emojis = emojis;
                  return this;
            }

            public Builder setJDA(JDA jda)
            {
                  this.jda = jda;
                  return this;
            }

            public Builder setDeleteOnTimeout(boolean behavior)
            {
                  this.deleteOnTimeOut = behavior;
                  return this;
            }

            public Builder setChannel(MessageChannel channel)
            {
                  this.channelId = channel.getIdLong();
                  return this;
            }

            public Builder setChannel(long id)
            {
                  this.channelId = id;
                  return this;
            }

            public Builder setEmbeds(List<MessageEmbed> embeds)
            {
                  this.embeds = embeds;
                  return this;
            }

            public Builder setTimeout(int timeout)
            {
                  this.timeout = timeout;
                  return this;
            }

            public Builder setTimeoutUnit(TimeUnit timeoutUnit)
            {
                  this.timeUnit = timeoutUnit;
                  return this;
            }

            public Builder setWaiter(EventWaiter waiter)
            {
                  this.eventWaiter = waiter;
                  return this;
            }

            private void validate()
            {
                  Checks.notEmpty(embeds, "Embeds");
                  Checks.notNull(eventWaiter, "Event Waiter");
                  Checks.notNull(jda, "JDA");
                  Checks.notNegative(channelId, "Channel ID");
            }

            @Nonnull
            public ButtonPaginator build()
            {
                  validate();
                  return new ButtonPaginator(this);
            }
      }


}
