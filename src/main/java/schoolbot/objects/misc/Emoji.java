package schoolbot.objects.misc;

public enum Emoji
{
      WHITE_CHECK_MARK(":white_check_mark:", "\u2705", false),
      CROSS_MARK(":x:", "\u274C", false),
      RECYCLE(":recycle:", "\u267B", false),
      TRACK_NEXT(":track_next:", "\u23ED", false),
      TRACK_PREVIOUS(":track_previous", "\u23EE", false),
      SMILEY_FACE(":slight_smile:", "\u1F642", false),
      ARROW_LEFT(":arrow_left:", "\u2B05", false),
      ARROW_RIGHT("arrow_right:", "\u27A1", false),
      BOOKS(":books: ", "\u1F4DA", false),
      RED_CIRCLE(":red_circle:", "\uF534", true),
      BLUE_CIRCLE(":blue_circle:", "\uF535", true),
      ORANGE_CIRCLE(":orange_circle:", "\uF7E0", true),
      WHITE_CIRCLE(":white_circle:", "\u26AA", true),
      PURPLE_CIRCLE(":purple_circle:", "\u26AA", true),
      BLACK_CIRCLE(":black_circle:", "\u26AB", true),
      HOURGLASS(":hourglass:", "\u231B", false),
      STOPWATCH(":stopwatch:", "\u23F1", false),
      WARNING(":warning:", "\u26A0", false);


      private final String emote;
      private final String unicode;
      private final boolean animated;
      // This can probably lead to confusion so let me clarify. When I mean pagniateReactable I mean if an emote is marked as <b> true </b>
      // It will be able to be listed on a paginator if you wanted to vote and use emojis as options
      private final boolean paginateReactable;


      Emoji(String emote, String unicode, boolean reactable)
      {
            this.emote = emote;
            this.unicode = unicode;
            this.animated = false;
            this.paginateReactable = reactable;
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

      public boolean isPaginateReactable()
      {
            return paginateReactable;
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
