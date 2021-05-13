package schoolbot.commands.admin;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.util.Checks;
import schoolbot.natives.util.Embed;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClearUser extends Command
{


      public ClearUser(Command parent)
      {
            super(parent, "", "", 1);

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
                        if (message.getMentionedMembers().size() == 0)
                        {
                              Embed.error(event, "You did not mention anyone");
                              return;
                        }


                        Member member = message.getMentionedMembers().get(0);
                        channel.getIterableHistory()
                                .takeAsync(100)
                                .thenApplyAsync(messages ->
                                {
                                      List<Message> channelMessages = messages.stream()
                                              .filter(deleteAbleMessages -> Objects.equals(deleteAbleMessages.getMember(), member))
                                              .filter(deleteAbleMessages -> deleteAbleMessages.getTimeCreated().isAfter(OffsetDateTime.now().minus(24, ChronoUnit.HOURS)))
                                              .limit(26)
                                              .collect(Collectors.toList());
                                      channel.purgeMessages(channelMessages);
                                      return channelMessages.size();
                                })
                                .whenCompleteAsync((channelMessageSize, throwable) ->
                                {
                                      if (channelMessageSize == 0)
                                      {
                                            Embed.error(event, "There were no messages to clear for " + member.getAsMention());
                                      }
                                      else
                                      {
                                            channel.sendMessage(
                                                    "Successfully purged `" + channelMessageSize + "` messages.")
                                                    .queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS));
                                      }
                                });
                  }

                  case 2 -> {
                        if (!Checks.isNumber(args.get(1)))
                        {
                              Embed.error(event, "[ ** %s ** ] is not a number", args.get(1));
                              return;
                        }


                        if (message.getMentionedMembers().size() == 0)
                        {
                              Embed.error(event, "You do not mention anyone...");
                              return;
                        }

                        int messagesToRemove = Integer.parseInt(args.get(1));

                        if (messagesToRemove > 100)
                        {
                              Embed.error(event, "You must choose a number between 1-100");
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
                                              .filter(deleteAbleMessages -> deleteAbleMessages.getTimeCreated().isAfter(OffsetDateTime.now().minus(24, ChronoUnit.HOURS)))
                                              .limit(messagesToRemove + 1)
                                              .collect(Collectors.toList());
                                      channel.purgeMessages(channelMessages);
                                      return channelMessages.size();
                                }).whenCompleteAsync((channelMessageSize, throwable) ->
                        {
                              if (channelMessageSize == 0)
                              {
                                    Embed.error(event, "There were no messages to clear for " + member.getAsMention());
                              }
                              else
                              {
                                    channel.sendMessage(
                                            "Successfully purged `" + channelMessageSize + "` messages.")
                                            .queue(botMessage -> botMessage.delete().queueAfter(5, TimeUnit.SECONDS));
                              }
                        });
                  }
            }
      }
}
