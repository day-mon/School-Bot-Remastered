// Package
package schoolbot.commands.admin;

// Imports

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Clear extends Command
{

      public Clear()
      {
            super("Clears messages in the text channel that the command was executed in if a number is not specified it will delete the last 25 messages", "[number]", 0);
            addCalls("purge", "clean", "clear");
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MESSAGE_MANAGE);
            addUsageExample("clear");
            addChildren(
                    new ClearUser(this)
            );
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            if (args.isEmpty())
            {

                  var channel = event.getChannel();
                  var message = event.getMessage();
                  var author = event.getUser();

                  message.delete().queue();


                  if (!message.isFromGuild())
                  {
                        author.openPrivateChannel()
                                .flatMap(privateChannel -> privateChannel.sendMessage(
                                        "This command must be used in a guild."
                                )).queue();

                        return;
                  }


                  channel.sendMessage(
                          "You are about to delete 100 messages, click the checkmark to continue, click the X to cancel."
                  ).queue(prompt ->
                  {
                        prompt.addReaction("\u2705").queue(); // Checkmark.
                        prompt.addReaction("\u274E").queue(); // X-Mark.

                        event.getSchoolbot().getEventWaiter().waitForEvent(MessageReactionAddEvent.class,
                                reactionEvent -> reactionEvent.getMessageIdLong() == prompt.getIdLong()
                                                 && event.getMember().getIdLong() == reactionEvent.getMember().getIdLong(),

                                reactionEvent ->
                                {
                                      switch (reactionEvent.getReaction().getReactionEmote().getName())
                                      {
                                            case "\u2705" -> prompt.getChannel().getIterableHistory()
                                                    .takeAsync(100)
                                                    .thenApplyAsync(channelMessages ->
                                                    {
                                                          List<Message> deletableMessages = channelMessages.stream()
                                                                  .filter(messages -> messages.getTimeCreated().isBefore(
                                                                          OffsetDateTime.now().plus(2, ChronoUnit.WEEKS)
                                                                  )).collect(Collectors.toList());

                                                          channel.purgeMessages(deletableMessages);

                                                          return deletableMessages.size();
                                                    }).whenCompleteAsync((messagesTotal, throwable) -> channel.sendMessage(
                                                            "Successfully purged `" + messagesTotal + "` messages."
                                                    ).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));
                                            case "\u274E" -> channel.sendMessage("Operation was successfully cancelled.").queue();
                                            default -> channel.sendMessage("You did not select one of the available options.").queue();
                                      }
                                }, 15, TimeUnit.SECONDS, () ->
                                {
                                      prompt.delete().queue();

                                      channel.sendMessage("You did not respond in time.").queue();
                                });
                  });

            }
            else
            {
                  var numCheck = args.get(0);
                  if (!Checks.isNumber(numCheck))
                  {
                        EmbedUtils.error(event, "This is not a number! " +
                                                "\nUsage: " + this.getUsageExample());
                        return;
                  }
                  int messagesToRemove = Integer.parseInt(numCheck);
                  event.getChannel().getIterableHistory()
                          .takeAsync(messagesToRemove)
                          .thenApplyAsync(channelMessages ->
                          {
                                List<Message> filteredDeletedMessages = channelMessages.stream()
                                        .filter(messages -> messages.getTimeCreated().isBefore(
                                                OffsetDateTime.now().plus(2, ChronoUnit.WEEKS)
                                        )).collect(Collectors.toList());
                                event.getChannel().purgeMessages(filteredDeletedMessages);
                                return filteredDeletedMessages.size();
                          }).whenCompleteAsync((messagesTotal, throwable) -> event.getChannel().sendMessageEmbeds(
                          new EmbedBuilder()
                                  .setDescription("Successfully purged `" + messagesTotal + "` messages.")
                                  .setColor(Color.GREEN)
                                  .build()).queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS)));
            }
      }
}
