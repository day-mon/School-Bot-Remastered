package schoolbot.natives.objects.misc;

public enum Emoji
{
      WHITE_CHECK_MARK(":white_check_mark:", "\u2705"),
      CROSS_MARK(":x:", "\u274C"),
      RECYCLE(":recycle:", "\u267B"),
      TRACK_NEXT(":track_next:", "\u23ED"),
      TRACK_PREVIOUS(":track_previous", "\u23EE"),
      SMILEY_FACE(":slight_smile:", "\u1F642"),
      ARROW_LEFT(":arrow_left", "\u2B05"),
      ARROW_RIGHT("arrow_right", "\u27A1"),
      BOOKS(":books: ", "\u1F4DA");


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
                  if (this.animated)
                  {
                        /**
                         * Animated emotes need <a before it.
                         */
                        return "<a:emote:" + this.emote + ">";
                  }
                  /**
                   * Non animated emote;
                   */
                  return "<:emote:" + this.emote + ">";
            }
            return this.emote;
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
