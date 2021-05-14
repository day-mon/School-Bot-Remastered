package schoolbot.commands.school;

import net.dv8tion.jda.api.Permission;
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
import java.util.stream.Collectors;

public class ProfessorRemove extends Command
{
      /**
       * @param parent
       */
      public ProfessorRemove(Command parent)
      {
            super(parent, "", "", 1);
            addPermissions(Permission.ADMINISTRATOR);
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            String arg0 = args.get(0);
            Member member = event.getMember();
            MessageChannel channel = event.getChannel();

            List<School> schoolList = event.getGuildSchools()
                    .stream()
                    .filter(school -> school.getProfessorList().size() < 1)
                    .collect(Collectors.toList());

            if (schoolList.isEmpty())
            {
                  Embed.error(event, "[ ** %s ** ] has no schools with deletable professors", event.getGuild().getName());
                  return;
            }
            else if (schoolList.size() == 1)
            {
                  event.getJDA().addEventListener(new ProfessorRemoveStateMachine(event, schoolList));
            }
            else
            {

            }


      }


      public static class ProfessorRemoveStateMachine extends ListenerAdapter
      {
            private CommandEvent commandEvent;
            private List<School> school;

            public ProfessorRemoveStateMachine(CommandEvent commandEvent, List<School> school)
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

