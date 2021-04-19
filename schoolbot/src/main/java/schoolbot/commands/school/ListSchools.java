package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.DatabaseUtil;
import schoolbot.natives.util.Embed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListSchools extends Command
{
    public ListSchools(EventWaiter waiter)
    {
        super("List schools all schools in database", "[none]", 0);
        addCalls("schools", "school-list");
        addCooldown(10000L);

    }

    @Override
    public void run(CommandEvent event)
    {
        List<School> schools = DatabaseUtil.getSchools(event.getSchoolbot())
                .stream()
                .filter(school -> event.getGuild().getIdLong() == school.getGuildID())
                .collect(Collectors.toList());

        if (schools.isEmpty())
        {
            Embed.error(event, "No schools!");
            return;
        }


        ArrayList<MessageEmbed> embeds = new ArrayList<>();


        for (School s : schools)
        {
            embeds.add(new EmbedBuilder()
                    .setTitle(s.getSchoolName())
                    .addField("Email Suffix(s)", s.getEmailSuffix(), false)
                    .addField("Role", event.getJDA().getRoleById(s.getRoleID()) == null ? "Role has been removed" : event.getJDA().getRoleById(s.getRoleID()).getAsMention(), false)
                    .setTimestamp(Instant.now())
                    .build());
        }


        ArrayList<Page> pages = new ArrayList<>();
        MessageBuilder mb = new MessageBuilder();

        for (MessageEmbed em : embeds)
        {
            mb.clear();
            pages.add(new Page(PageType.EMBED, em));
        }

        event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent()).queue(success ->
        {
            Pages.paginate(success, pages);
        });


    }
}
