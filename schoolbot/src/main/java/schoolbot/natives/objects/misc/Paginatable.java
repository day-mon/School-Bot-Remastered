package schoolbot.natives.objects.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import schoolbot.Schoolbot;

public interface Paginatable
{
      MessageEmbed getAsEmbed(Schoolbot schoolbot);

      EmbedBuilder getAsEmbedBuilder(Schoolbot schoolbot);
}
