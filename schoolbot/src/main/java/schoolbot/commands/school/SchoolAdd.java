package schoolbot.commands.school;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import schoolbot.SchoolbotConstants;
import schoolbot.handlers.ConfigHandler;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.config.ConfigOption;
import schoolbot.natives.util.Embed;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Objects;

public class SchoolAdd extends Command
{
    private final EventWaiter waiter;
    public SchoolAdd(Command parent, EventWaiter waiter)
    {
        super(parent,"Adds a school to the server", "[school name] [school email suffix] [school reference]", 0);
        this.waiter = waiter;
    }


    @Override
    public void run(CommandEvent event)
    {


        MessageChannel channel = event.getChannel();
        User user = event.getUser();
        channel.sendMessage("Hello " + event.getMember().getNickname() + " You would like to add a school? What is this name?: ")
                .queue(reply ->
                {

                    this.waiter.waitForEvent(MessageReceivedEvent.class,
                            messageReceivedEvent ->
                            {
                                if (!Objects.equals(messageReceivedEvent.getAuthor(), event.getUser()))
                                {
                                    return false;
                                }

                                if (!Objects.equals(messageReceivedEvent.getChannel(), channel))
                                {
                                    return false;
                                }

                                return true;
                            },
                            action ->
                            {
                                if (!action.getMessage().getContentRaw().replaceAll("\\s+", "").chars().allMatch(Character::isAlphabetic))
                                {
                                    Embed.error(event, "School name cannot contain non-alphabetic characters!\n SchoolAdd has been aborted!");
                                    return;
                                }
                                channel.sendMessage("Your school name will be: " + action.getMessage().getContentRaw()).queue();

                                channel.sendMessage("Now that we have your school name, What is your email suffix (i.e @pitt.edu): ").queue(v -> {
                                    this.waiter.waitForEvent(MessageReceivedEvent.class,
                                            messageReceivedEvent ->
                                            {
                                                if (!Objects.equals(messageReceivedEvent.getAuthor(), event.getUser()))
                                                {
                                                    return false;
                                                }

                                                if (!Objects.equals(messageReceivedEvent.getChannel(), channel))
                                                {
                                                    return false;
                                                }

                                                return true;                                            },
                                            action1 ->
                                            {
                                                if(!action1.getMessage().getContentRaw().contains("edu"))
                                                {
                                                    Embed.error(event, "This is not a valid email!");
                                                    return;
                                                }
                                                channel.sendMessage("Your school email will be: " + action1.getMessage().getContentRaw()).queue();

                                                String sxx = String.format("('%s', %d, '%s', %d)",
                                                        action.getMessage().getContentRaw(), event.getChannel().getIdLong(), action1.getMessage().getContentRaw(), event.getChannel().getIdLong());

                                                /*
                                                if (event.getSchoolbot().getDatabaseHandler().writeToTable("schools (school_id, role_id, school_email_suffix, guild id)", sxx))
                                                {
                                                    channel.sendMessage("Database updated successfully!").queue();
                                                    channel.sendMessage(new EmbedBuilder()
                                                            .setTitle("New School Created!")
                                                            .addField("School name", action.getMessage().getContentRaw(), false)
                                                            .addField("School Email", action1.getMessage().getContentRaw(), false)
                                                            .setFooter("Created by " + user.getName() ,user.getAvatarUrl())
                                                            .setTimestamp(Instant.now())
                                                            .setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR)
                                                            .build()).queue();
                                                }
                                                else
                                                {
                                                    channel.sendMessage("Database could not be updated, view the console").queue();
                                                }
                */



                                            }




                                            );
                                }); //send message here?
                            });
                });

    }



}
