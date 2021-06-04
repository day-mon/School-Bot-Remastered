package schoolbot.commands.school;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.school.Professor;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.Embed;

import java.util.List;
import java.util.stream.Collectors;

public class ListProfessors extends Command
{
      public ListProfessors()
      {
            super("", "", 0);
            addCalls("professors", "profs", "lp");
            addFlags(CommandFlag.DATABASE);
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {
            List<School> schools = event.getGuildSchools()
                    .stream()
                    .filter(school -> !school.getProfessorList().isEmpty())
                    .collect(Collectors.toList());

            if (schools.isEmpty())
            {
                  Embed.error(event, "** %s ** has no schools..", event.getGuild().getName());
            }
            else if (schools.size() == 1)
            {
                  School school = schools.get(0);
                  Embed.information(event, "Showing professors from %s because they are the only school with professors in this server", school.getName());
                  event.getProfessorsAsPaginator(school);
            }
            else
            {
                  event.getAsPaginatorWithPageNumbers(schools);
                  Embed.information(event, "Please choose a page number from the Paginator");
                  event.getJDA().addEventListener(new ListProfessorStateMachine(event, schools));
            }
      }

      public static class ListProfessorStateMachine extends ListenerAdapter
      {
            private final long channelID, authorID;
            private final List<School> schools;
            private final CommandEvent commandEvent;

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
                  if (!Checks.between(Integer.parseInt(event.getMessage().getContentRaw()), schools.size())) return;
                  // Could put error messages between here...

                  String content = event.getMessage().getContentRaw();


                  int index = Integer.parseInt(content) - 1;

                  List<Professor> professorList = schools.get(index).getProfessorList();

                  if (professorList.isEmpty())
                  {
                        Embed.error(event, "** %s ** has no professors..", schools.get(index).getName());
                        return;
                  }
                  else if (professorList.size() == 1)
                  {
                        event.getChannel().sendMessage(professorList.get(0).getAsEmbed(commandEvent.getSchoolbot())).queue();
                        event.getJDA().removeEventListener(this);
                        return;
                  }

                  commandEvent.getProfessorsAsPaginator(schools.get(index));
                  event.getJDA().removeEventListener(this);


            }
      }

}
