package schoolbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Constants;
import schoolbot.Schoolbot;
import schoolbot.handlers.CommandCooldownHandler;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Emoji;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EmbedUtils
{
      private static final Logger LOGGER = LoggerFactory.getLogger(EmbedUtils.class);

      private EmbedUtils()
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

      public static void warn(CommandEvent event, String message, Object... args)
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
                    .setDescription(String.format(message, args) + "\n You may use 'exit' or 'stop' at any time to exit this command")
            );
      }

      public static <T extends MessageChannel> void sendTutorial(T channel, Schoolbot schoolbot, Guild guild)
      {

            var prefix = schoolbot.getWrapperHandler().fetchGuildPrefix(guild.getIdLong());

            var embedDescription = String.format("""
                    Thank you for adding Schoolbot to your server!
                                        
                    To begin: You can call %scommands to see all my commands.
                                        
                    Below is a tutorial on how to use the main selling point of the bot. All school commands have a Add, Edit, and Remove implemented for them.
                    z
                    If you have any questions about any of these commands you can simple call %1$shelp <command name> also, if you do not know the bots prefix for your guild you can mention it!
                    """, prefix);
            channel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("Tutorial")
                            .setDescription(embedDescription)
                            .addField("Step 1", "Use " + prefix + "school add and read all of the instructions", false)
                            .addField("Step 2", "You now have a school. You will need a " + prefix + "professor for this school. Call professor add to do so, and follow the instructions. If you goto a University of Pittsburgh School you can bypass this step!", false)
                            .addField("Step 3", String.format("""
                                    Now that you have a Professor. You can use %sclass add. If you attend a University of Pittsburgh Campus I have a special system.
                                    During this process I will add class reminders, create a role and a text channel
                                    """, prefix), false)
                            .addField("Step 4", "Now that you have a class. You can call " + prefix + "assignment add. This will add reminders and send them in the corresponding channels", false)
                            .addField("Final Remarks", "Now when you have done all of those steps. To add additional schools, classes, professors, and assignments the process is the same! If you need help again you can just call " + prefix + "tutorial again!. Also I count everytime you use space as an individual argument, use the ' character to keep things you would like to use spaces with as one argument", false)
                            .build()
            ).queue(null, failure ->
                    LOGGER.error("Could not send tutorial in any channel", failure));
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

      public static void confirmation(CommandEvent event, String message, Consumer<MessageReactionAddEvent> doAfterConsumer, Object... args)
      {
            var eventWaiter = event.getSchoolbot().getEventWaiter();

            event.getChannel().sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("Confirmation")
                            .setDescription(String.format(message, args))
                            .build()
            ).queue(consumerMessage ->
            {
                  consumerMessage.addReaction(Emoji.WHITE_CHECK_MARK.getAsReaction()).queue();
                  consumerMessage.addReaction(Emoji.CROSS_MARK.getAsReaction()).queue();

                  eventWaiter.waitForEvent(MessageReactionAddEvent.class,
                          messageReceivedEvent -> messageReceivedEvent.getMessageIdLong() == consumerMessage.getIdLong()
                                                  && messageReceivedEvent.getMember().getIdLong() == event.getMember().getIdLong()
                                                  && (messageReceivedEvent.getReaction().getReactionEmote().getName().equals(Emoji.WHITE_CHECK_MARK.getUnicode())
                                                      || messageReceivedEvent.getReaction().getReactionEmote().getName().equals(Emoji.CROSS_MARK.getUnicode())), doAfterConsumer);
            });
      }

}
