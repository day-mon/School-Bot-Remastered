package schoolbot.natives.objects.misc;

public enum Emoji 
{
    WHITE_CHECK_MARK(":white_check_mark:", "\u2705"),
    CROSS_MARK(":x:", "\u274C"),
    RECYCLE(":recycle:", "\u267B");
    

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
