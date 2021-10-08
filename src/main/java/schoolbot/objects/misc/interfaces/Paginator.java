package schoolbot.objects.misc.interfaces;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.concurrent.TimeUnit;

public interface Paginator
{
      int DEFAULT_TIMEOUT = 10;
      TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;
      boolean DEFAULT_DELETE_ON_TIMEOUT = true;

      /*
      Button previous = Button.secondary("previous", "Previous Page");
      Button stop = Button.danger("stop", "Exit");
      Button next = Button.secondary("next", "Next Page");
      Button end = Button.primary("end", "Last Page");
      Button start = Button.primary("start", "First Page");

       */
      Button previous = Button.secondary("previous", Emoji.fromUnicode("⬅"));
      Button stop = Button.danger("stop", Emoji.fromUnicode("\uD83D\uDDD1"));
      Button next = Button.secondary("next", Emoji.fromUnicode("➡"));
      Button end = Button.secondary("end", Emoji.fromUnicode("⏩"));
      Button start = Button.secondary("start", Emoji.fromUnicode("⏪"));



      void paginate();

      int getTimeout();

      TimeUnit getTimeoutUnit();

      int getPage();
}
