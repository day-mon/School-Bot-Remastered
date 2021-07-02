package schoolbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Constants;
import schoolbot.handlers.CommandCooldownHandler;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class EmbedUtils
{
      private static final Logger LOGGER = LoggerFactory.getLogger(EmbedUtils.class);

      public EmbedUtils()
      {
      }

      public static void success(CommandEvent event, String message)
      {
            MessageChannel channel = event.getChannel();

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setDescription(message)
                    .build()).queue();
      }

      public static void success(GuildMessageReceivedEvent event, String message, Object... args)
      {
            var channel = event.getChannel();

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(Emoji.WHITE_CHECK_MARK.getAsChat() + " Success " + Emoji.WHITE_CHECK_MARK.getAsChat())
                    .setColor(Color.GREEN)
                    .setDescription(String.format(message, args))
                    .build())
                    .queue(null, failure ->
                    {
                          var user = event.getAuthor();

                          user.openPrivateChannel()
                                  .flatMap(privateMessage -> privateMessage.sendMessageEmbeds(new EmbedBuilder()
                                          .setTitle(Emoji.WHITE_CHECK_MARK.getAsChat() + " Success " + Emoji.WHITE_CHECK_MARK.getAsChat())
                                          .setColor(Color.GREEN)
                                          .setDescription(String.format(message, args))
                                          .build())).queue();
                    });


      }

      public static void success(CommandEvent event, String message, Object... args)
      {
            var channel = event.getTextChannel();


            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(Emoji.WHITE_CHECK_MARK.getAsChat() + " Success " + Emoji.WHITE_CHECK_MARK.getAsChat())
                    .setColor(Color.GREEN)
                    .setDescription(String.format(message, args))
                    .build())
                    .queue(null, failure ->
                    {
                          var user = event.getUser();

                          user.openPrivateChannel()
                                  .flatMap(privateMessage -> privateMessage.sendMessageEmbeds(new EmbedBuilder()
                                          .setTitle(Emoji.WHITE_CHECK_MARK.getAsChat() + " Success " + Emoji.WHITE_CHECK_MARK.getAsChat())
                                          .setColor(Color.GREEN)
                                          .setDescription(String.format(message, args))
                                          .build())).queue();
                    });

      }

      public static void error(CommandEvent event, String message)
      {
            event.sendMessage(new EmbedBuilder()
                    .setTitle(Emoji.CROSS_MARK.getAsChat() + " Error " + Emoji.CROSS_MARK.getAsChat())
                    .setColor(Color.RED)
                    .setDescription(message)
                    .build());
      }

      public static void error(GuildMessageReceivedEvent event, String message)
      {
            MessageChannel channel = event.getChannel();

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(Emoji.CROSS_MARK.getAsChat() + " Error " + Emoji.CROSS_MARK.getAsChat())
                    .setColor(Color.RED)
                    .setDescription(message)
                    .build()).queue();
      }

      public static void notANumberError(GuildMessageReceivedEvent event, String message)
      {
            error(event, "** %s ** is not a number. Please retry with a valid entry", message);
      }


      public static void error(GuildMessageReceivedEvent event, String message, Object... args)
      {
            MessageChannel channel = event.getChannel();

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(Emoji.CROSS_MARK.getAsChat() + " Error " + Emoji.CROSS_MARK.getAsChat())
                    .setColor(Color.RED)
                    .setDescription(String.format(message, args))
                    .build()).queue();
      }

      public static void warn(GuildMessageReceivedEvent event, String message, Object... args)
      {
            var channel = event.getChannel();

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(Emoji.WARNING.getAsChat() + " Warning " + Emoji.WARNING.getAsChat()).setColor(Color.YELLOW)
                    .setDescription(String.format(message, args))
                    .build()).queue();
      }

      public static void warn(GuildMessageReceivedEvent event, String message)
      {
            var channel = event.getChannel();

            channel.sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(Emoji.WARNING.getAsChat() + " Warning " + Emoji.WARNING.getAsChat())
                    .setColor(Color.YELLOW)
                    .setDescription(message)
                    .build()).queue();
      }


      public static void error(CommandEvent event, String message, Object... args)
      {
            event.sendMessage(new EmbedBuilder()
                    .setTitle(Emoji.CROSS_MARK.getAsChat() + " Error " + Emoji.CROSS_MARK.getAsChat())
                    .setColor(Color.RED)
                    .setDescription(String.format(message, args))
                    .build());
      }

      public static void sendInvalidMemberPermissions(CommandEvent event)
      {
            List<Permission> permissionsMemberHas = event.getCommand().getCommandPermissions()
                    .stream()
                    .filter(perm -> !event.getMember().hasPermission(perm))
                    .collect(Collectors.toList());

            StringBuilder perms = new StringBuilder();
            permissionsMemberHas.forEach(permission -> perms.append(" `")
                    .append(permission)
                    .append("` "));

            event.sendMessage("This command requires you to have at least this permission" + perms + "in order to execute it!");
      }

      public static void sendIsOnCooldown(CommandEvent event)
      {
            int cooldownTime = CommandCooldownHandler.getCooldownTime(event.getMember(), event.getCommand());
            event.sendMessage("You are on a cooldown \nYou have [" + cooldownTime + "] ***second(s)***");
      }

      public static void information(CommandEvent event, String message)
      {
            event.sendMessage(new EmbedBuilder()
                    .setTitle("Important Information")
                    .setDescription(message)
            );
      }

      public static void information(CommandEvent event, String message, Object... args)
      {
            event.sendMessage(new EmbedBuilder()
                    .setTitle("Important Information")
                    .setDescription(String.format(message, args))
            );
      }

      public static <T extends MessageChannel> void sendTutorial(T channel)
      {
            var embedDescription = String.format("""
                    Thank you for adding Schoolbot to your server!
                                        
                                      
                    """);
            channel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("Tutorial")
                            .setDescription(embedDescription)
                            .addField("Step 1", "To use wha")
                            .build()
            ).queue(null, failure ->
            {
            });
      }


      public static void information(GuildMessageReceivedEvent event, String message, Object... args)
      {
            event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                    .setTitle("Important Information")
                    .setDescription(String.format(message, args))
                    .setColor(Constants.DEFAULT_EMBED_COLOR)
                    .build()
            ).queue();
      }

}
