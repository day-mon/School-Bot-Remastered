package schoolbot.natives.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import schoolbot.handlers.CommandCooldownHandler;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.misc.Emoji;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class Embed
{

      public Embed()
      {
      }

      public static void success(CommandEvent event, String message)
      {
            MessageChannel channel = event.getChannel();

            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setDescription(message)
                    .build()).queue();
      }

      public static void success(GuildMessageReceivedEvent event, String message, Object... args)
      {
            MessageChannel channel = event.getChannel();

            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setDescription(Emoji.WHITE_CHECK_MARK.getAsChat() + " " + String.format(message, args))
                    .build()).queue();
      }

      public static void error(CommandEvent event, String message)
      {


            event.sendMessage(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription(message)
                    .build());
      }

      public static void error(GuildMessageReceivedEvent event, String message)
      {
            MessageChannel channel = event.getChannel();

            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription(Emoji.CROSS_MARK.getAsChat() + " " + message)
                    .build()).queue();
      }

      public static void error(GuildMessageReceivedEvent event, String message, Object... args)
      {
            MessageChannel channel = event.getChannel();

            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription(Emoji.CROSS_MARK.getAsChat() + " " + String.format(message, args))
                    .build()).queue();
      }

      public static void error(CommandEvent event, String message, Object... args)
      {
            event.sendMessage(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription(Emoji.CROSS_MARK.getAsChat() + " " + String.format(message, args))
                    .build());
      }

      public static void sendInvalidMemberPermissions(CommandEvent event)
      {
            List<Permission> permissionsMemberHas = event.getCommand().getCommandPermissions()
                    .stream()
                    .filter(perm -> !event.getMember().hasPermission(perm))
                    .collect(Collectors.toList());

            StringBuilder perms = new StringBuilder();
            permissionsMemberHas.forEach(f -> perms.append(" `").append(f).append("` "));

            event.sendMessage("This command requires you to have at least this permission" + perms.toString() + "in order to execute it!");
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


      public static void confirmation(CommandEvent event, String message)
      {
            event.sendMessage(new EmbedBuilder()
                    .setDescription(message));

      }
}
