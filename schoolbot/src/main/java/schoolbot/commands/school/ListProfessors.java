package schoolbot.commands.school;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.command.CommandFlag;
import schoolbot.natives.objects.school.Professor;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Embed;

import java.util.List;

public class ListProfessors extends Command
{
      public ListProfessors()
      {
            super("", "", 0);
            addCalls("professors", "profs", "lp");
            addFlags(CommandFlag.DATABASE);
      }

      @Override
      public void run(CommandEvent event)
      {
            List<School> schools = event.getGuildSchools();

            if (schools.isEmpty())
            {
                  Embed.error(event, "** %s ** has no schools..", event.getGuild().getName());
                  return;
            }
            else if (schools.size() == 1)
            {
                  event.getProfessorsAsPaginator(schools.get(0));
                  return;
            }


            event.getSchoolsAsPaginator();
            Embed.information(event, "Please choose a page number from the Paginator");
            event.getJDA().addEventListener(new ListProfessorStateMachine(event, schools));

      }

      public static class ListProfessorStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private List<School> schools;
            private CommandEvent commandEvent;

            public ListProfessorStateMachine(CommandEvent event, List<School> schools)
            {
                  this.authorID = event.getUser().getIdLong();
                  this.channelID = event.getChannel().getIdLong();
                  this.schools = schools;
                  this.commandEvent = event;

            }

            @Override
            public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event)
            {
                  if (event.getAuthor().getIdLong() != authorID) return;
                  if (event.getChannel().getIdLong() != channelID) return;
                  if (!event.getMessage().getContentRaw().chars().allMatch(Character::isDigit)) return;
                  // Could put error messages between here...
                  if (!between(Integer.parseInt(event.getMessage().getContentRaw()), 1, schools.size())) return;
                  // Could put error messages between here...

                  String content = event.getMessage().getContentRaw();


                  int index = Integer.parseInt(content) - 1;

                  List<Professor> professorList = schools.get(index).getProfessorList();

                  if (professorList.isEmpty())
                  {
                        Embed.error(event, "** %s ** has no professors..", schools.get(index).getSchoolName());
                        return;
                  }
                  else if (professorList.size() == 1)
                  {
                        event.getChannel().sendMessage(professorList.get(0).getAsEmbed()).queue();
                        event.getJDA().removeEventListener(this);
                        return;
                  }

                  commandEvent.getProfessorsAsPaginator(schools.get(index));
                  event.getJDA().removeEventListener(this);


            }

            private static boolean between(int i, int minValueInclusive, int maxValueInclusive)
            {
                  return (i >= minValueInclusive && i <= maxValueInclusive);
            }
      }

}
