package schoolbot.handlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.dv8tion.jda.api.entities.Member;
import schoolbot.natives.objects.command.Command;

public class CommandCooldownHandler 
{

    /**
     * Used for thread safety.
     */
    private static final Map<CooledCommand, Long> COOLDOWN_MAP = new ConcurrentHashMap<>();


    private CommandCooldownHandler(){};

    public static boolean isOnCooldown(Member member, Command command)
    {
        long userID = member.getIdLong();
        long guildID = member.getGuild().getIdLong();

        for (Map.Entry<CooledCommand, Long> entry : COOLDOWN_MAP.entrySet())
		{
			CooledCommand cooledCommand = entry.getKey();
			long expiry = entry.getValue();

			if (cooledCommand.getUserID() == userID && cooledCommand.getGuildID() == guildID && cooledCommand.getCommand().equals(command))
			{
				if (System.currentTimeMillis() <= expiry)
				{
					return true;
				}
				COOLDOWN_MAP.remove(cooledCommand);
				return false;
			}
		}
		return false;
    }

    public static void addCooldown(Member member, Command command)
    {
        /**
         * Will put a CoolCommand object
         *      Contains: userID
         *                guildID
         *                Command
         * With the current time + the cooldown/
         */
        COOLDOWN_MAP.put(new CooledCommand(member, command), System.currentTimeMillis() + command.getCooldown());
    }


    public static class CooledCommand
    {
        private final long userID;
        private final long guildID;
        private final Command command;

        public CooledCommand(Member member, Command command)
        {
             this.userID = member.getIdLong();
             this.guildID = member.getGuild().getIdLong();
             this.command = command;
        }

        public Command getCommand() {
            return command;
        }

        public long getGuildID() {
            return guildID;
        }

        public long getUserID() {
            return userID;
        }
    }
}