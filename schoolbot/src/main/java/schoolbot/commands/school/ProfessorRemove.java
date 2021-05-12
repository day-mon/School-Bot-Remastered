package schoolbot.commands.school;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Embed;

import java.util.List;

public class ProfessorRemove extends Command
{
      /**
       * @param parent
       */
      public ProfessorRemove(Command parent)
      {
            super(parent, "", "", 1);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String arg0 = args.get(0);
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();
            List<School> schoolList = event.getGuildSchools();

            if (schoolList.isEmpty())
            {
                  Embed.error(event, "[ ** %s ** ] has no schools", event.getGuild().getName());
                  return;
            }

      }


      public static class ProfessorRemoveStateMachine extends ListenerAdapter
      {
            private CommandEvent commandEvent;
            private School school;

            public ProfessorRemoveStateMachine(CommandEvent commandEvent, School school)
            {
                  this.commandEvent = commandEvent;
                  this.school = school;
            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {

            }
      }

}

