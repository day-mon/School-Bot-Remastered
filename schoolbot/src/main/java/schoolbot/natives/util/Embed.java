package schoolbot.natives.util;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.misc.Emoji;

public class Embed {

    public Embed() {};

    public static void success(CommandEvent event, String message) 
    {
        MessageChannel channel = event.getChannel();

        channel.sendMessage(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setDescription(message)
                            .build()).queue();
    }

    public static void error(CommandEvent event, String message) 
    {
        MessageChannel channel = event.getChannel();

        channel.sendMessage(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setDescription(message)
                            .build()).queue();
    }

    public static void error(GuildMessageReceivedEvent event, String message) 
    {
        MessageChannel channel = event.getChannel();

        channel.sendMessage(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setDescription(Emoji.CROSS_MARK.getAsChat()+message)
                            .build()).queue();
    }

    public static void confirmation(CommandEvent event, String message) 
    {
        MessageChannel channel = event.getChannel();

       channel.sendMessage(new EmbedBuilder()
                            .setColor(Color.GRAY)
                            .setDescription(message)
                            .build()).queue();;
                            

    
    }
}
