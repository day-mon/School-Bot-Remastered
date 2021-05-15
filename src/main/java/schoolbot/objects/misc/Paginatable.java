package schoolbot.objects.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import schoolbot.Schoolbot;

public interface Paginatable
{
      MessageEmbed getAsEmbed(@NotNull Schoolbot schoolbot);

      EmbedBuilder getAsEmbedBuilder(@NotNull Schoolbot schoolbot);
}
