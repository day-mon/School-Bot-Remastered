package schoolbot.commands.admin;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClearUser extends Command
{


      public ClearUser(Command parent)
      {
            super(parent, "Clears messages of a target user", "[user mention] <amount of messages>", 1);
            addPermissions(Permission.ADMINISTRATOR);
            addSelfPermissions(Permission.MESSAGE_MANAGE);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

            int argCount = args.size();
            Message message = event.getMessage();
            MessageChannel channel = event.getChannel();

            message.delete().queue();

            switch (argCount)
            {
                  case 1 -> {
                        if (message.getMentionedMembers().isEmpty())
                        {
                              EmbedUtils.error(event, "You did not mention anyone");
                              return;
                        }


                        Member member = message.getMentionedMembers().get(0);
                        channel.getIterableHistory()
                                .takeAsync(100)
                                .thenApplyAsync(messages ->
                                {
                                      List<Message> channelMessages = messages.stream()
                                              .filter(deleteAbleMessages -> Objects.equals(deleteAbleMessages.getMember(), member))
                                              .filter(deleteAbleMessages -> Duration.between(message.getTimeCreated(), deleteAbleMessages.getTimeCreated()).toHours() < 24)
                                              .limit(26)
                                              .collect(Collectors.toList());
                                      var newList = channelMessages.subList(1, channelMessages.size());

                                      event.getChannel().purgeMessages(newList);

                                      return newList.size();
                                })
                                .whenCompleteAsync((channelMessageSize, throwable) ->
                                {

                                      if (throwable != null)
                                      {
                                            EmbedUtils.error(event, "Error has occurred whilst trying to delete messages for " + member.getAsMention());
                                            return;
                                      }

                                      if (channelMessageSize == 0)
                                      {
                                            EmbedUtils.error(event, "There were no messages to clear for " + member.getAsMention());
                                            return;
                                      }


                                      channel.sendMessage(
                                              "Successfully purged `" + channelMessageSize + "` messages.")
                                              .queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS, null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER)));
                                });
                  }

                  case 2 -> {
                        if (!Checks.isNumber(args.get(1)))
                        {
                              EmbedUtils.error(event, "[ ** %s ** ] is not a number", args.get(1));
                              return;
                        }


                        if (message.getMentionedMembers().isEmpty())
                        {
                              EmbedUtils.error(event, "You do not mention anyone...");
                              return;
                        }

                        int messagesToRemove = Integer.parseInt(args.get(1));

                        if (messagesToRemove > 100 || messagesToRemove <= 0)
                        {
                              EmbedUtils.error(event, "You must choose a number between 1-100");
                              return;
                        }

                        Member member = message.getMentionedMembers().get(0);
                        channel.getIterableHistory()
                                .takeAsync(100)
                                .thenApplyAsync(messages ->
                                {
                                      List<Message> channelMessages = messages
                                              .stream()
                                              .filter(deleteAbleMessages -> Objects.equals(deleteAbleMessages.getMember(), member))
                                              .filter(deleteAbleMessages -> Duration.between(message.getTimeCreated(), deleteAbleMessages.getTimeCreated()).toHours() < 24)
                                              .limit(messagesToRemove + 1)
                                              .collect(Collectors.toList());

                                      var newList = channelMessages.subList(1, channelMessages.size());

                                      event.getChannel().purgeMessages(newList);

                                      return newList.size();
                                }).whenCompleteAsync((channelMessageSize, throwable) ->
                        {

                              if (throwable != null)
                              {
                                    EmbedUtils.error(event, "Error has occurred whilst trying to delete messages for " + member.getAsMention());
                                    return;
                              }

                              if (channelMessageSize == 0)
                              {
                                    EmbedUtils.error(event, "There were no messages to clear for " + member.getAsMention());
                                    return;
                              }

                              channel.sendMessage("Successfully purged `" + channelMessageSize + "` messages.")
                                      .queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS, null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)));

                        });
                  }
            }
      }
}
