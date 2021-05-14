package schoolbot.handlers;

import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.natives.objects.command.Command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandCooldownHandler
{

      private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

      public CommandCooldownHandler()
      {

      }


      /**
       * Used for thread safety.
       */
      private static final Map<CooledCommand, Long> COOLDOWN_MAP = new ConcurrentHashMap<>();


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

      public static int getCooldownTime(Member mem, Command command)
      {
            long userID = mem.getIdLong();
            long guildID = mem.getGuild().getIdLong();

            for (Map.Entry<CooledCommand, Long> entry : COOLDOWN_MAP.entrySet())
            {
                  CooledCommand cooledCommand = entry.getKey();
                  long expiry = entry.getValue();

                  if (cooledCommand.getUserID() == userID && cooledCommand.getGuildID() == guildID && cooledCommand.getCommand().equals(command))
                  {
                        return (int) ((int) (expiry / 1000) - (System.currentTimeMillis() / 1000));
                  }
            }
            return 0;
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

            public Command getCommand()
            {
                  return command;
            }

            public long getGuildID()
            {
                  return guildID;
            }

            public long getUserID()
            {
                  return userID;
            }

      }
}
