package schoolbot.objects.command;

import schoolbot.objects.misc.Emoji;

public enum CommandCategory
{
      ADMIN("Admin", Emoji.A),
      FUN("Fun", Emoji.FERRIS_WHEEL),
      INFO("Information", Emoji.TOOLS),
      DEV("Dev", Emoji.LAPTOP),
      UNKNOWN("Unknown", Emoji.WARNING),
      MISC("Misc", Emoji.MAN_SHRUG),
      SCHOOL("School", Emoji.BOOKS);

      private final String name;
      private final Emoji emoji;

      CommandCategory(String name, Emoji emoji)
      {
            this.name = name;
            this.emoji = emoji;
      }

      public Emoji getEmoji()
      {
            return emoji;
      }

      public String getName()
      {
            return name;
      }
}
