package schoolbot.objects.misc;

import java.util.List;

public enum Emoji
{
      WHITE_CHECK_MARK(":white_check_mark:", "\u2705"),
      CROSS_MARK(":x:", "\u274C"),
      RECYCLE(":recycle:", "\u267B"),
      TRACK_NEXT(":track_next:", "\u23ED"),
      TRACK_PREVIOUS(":track_previous", "\u23EE"),
      SMILEY_FACE(":slight_smile:", "\u1F642"),
      ARROW_LEFT(":arrow_left:", "\u2B05"),
      ARROW_RIGHT("arrow_right:", "\u27A1" ),
      BOOKS(":books: ", "\u1F4DA" ),
      RED_CIRCLE(":red_circle:", "\uF534" ),
      BLUE_CIRCLE(":blue_circle:", "\uF535" ),
      ORANGE_CIRCLE(":orange_circle:", "\uF7E0" ),
      WHITE_CIRCLE(":white_circle:", "\u26AA" ),
      PURPLE_CIRCLE(":purple_circle:", "\u26AA" ),
      BLACK_CIRCLE(":black_circle:", "\u26AB" ),
      HOURGLASS(":hourglass:", "\u231B" ),
      STOPWATCH(":stopwatch:", "\u23F1" ),
      WARNING(":warning:", "\u26A0" ),
      A(":a:", "\uF170" ),
      LAPTOP(":computer:", "\uF4BB"),
      TOOLS(":tools:", "\uF6E0"),
      FERRIS_WHEEL(":ferris_wheel:", "\uF3A1"),
      MAN_SHRUG(":man_shrugging:", "\uD83E\uDD37\u200D\u2642\uFE0F");


      private final String emote;
      private final String unicode;
      private final boolean animated;

      Emoji(String emote, String unicode)
      {
            this.emote = emote;
            this.unicode = unicode;
            this.animated = false;
      }

      public String getUnicode()
      {
            return unicode;
      }

      public String getAsReaction()
      {
            if (this.unicode.isBlank())
            {
                  return "emote:" + this.emote;
            }
            return this.unicode;
      }

      public List<Emoji> getReactables()
      {
            return List.of(
                    RED_CIRCLE,
                    BLUE_CIRCLE,
                    BLACK_CIRCLE,
                    ORANGE_CIRCLE,
                    PURPLE_CIRCLE
            );
      }

      /**
       * Method is for message reactions
       */
      public String getAsChat()
      {
            if (this.unicode.isBlank())
            {
                  if (this.animated)
                  {
                        return "<a:emote:" + this.emote + ">";
                  }
                  return "<:emote:" + this.emote + ">";
            }
            return this.emote;
      }
}
