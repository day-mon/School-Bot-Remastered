package schoolbot.commands.info;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.util.Embed;
import schoolbot.util.StringUtils;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class UserInfo extends Command
{

      public UserInfo()
      {
            super("Shows info about a user", "<user mention>", 0);
            addCalls("userinfo", "about");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            var channel = event.getChannel();

            if (args.isEmpty())
            {
                  Member member = event.getMember();

                  displayInfo(member, channel);
            }
            else
            {
                  List<Member> members = event.getMessage().getMentionedMembers();

                  if (members.isEmpty())
                  {
                        Embed.error(event, "There are no members mentioned in this message");
                        return;
                  }

                  if (members.size() > 1)
                  {
                        Embed.error(event, "Please only mention one member please.");
                        return;
                  }

                  Member member = members.get(0);

                  displayInfo(member, channel);
            }
      }


      private void displayInfo(Member member, MessageChannel channel)
      {
            var user = member.getUser();

            channel.sendMessage(new EmbedBuilder()
                    .setTitle("Information on **" + user.getName() + "** ")
                    .addField("Join date", StringUtils.formatDate(member.getTimeJoined()), false)
                    .addField("Boosting Since", member.getTimeBoosted() == null ? "Not Boosting" : StringUtils.formatDate(member.getTimeBoosted()), false)
                    .addField("Account Creation Date", StringUtils.formatDate(member.getTimeCreated()), false)
                    .addField("User ID", member.getId(), false)
                    .addField("Roles", member.getRoles().stream().map(Role::getAsMention).collect(Collectors.joining(", ")), false)
                    .setThumbnail(user.getAvatarUrl())
                    .setColor(Color.black)
                    .build()).queue();
      }
}
