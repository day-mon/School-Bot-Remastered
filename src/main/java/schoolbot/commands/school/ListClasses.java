package schoolbot.commands.school;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.school.School;
import schoolbot.util.Checks;
import schoolbot.util.EmbedUtils;

import java.util.List;
import java.util.stream.Collectors;

public class ListClasses extends Command
{
      public ListClasses()
      {
            super("List classes for given a target university", "<school name>", 0);
            addCalls("classes", "classlist");
            addUsageExample("classes 'University of Pittsburgh' or classes");
      }


      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args)
      {

            if (args.isEmpty())
            {
                  var schools = event.getGuildSchools()
                          .stream()
                          .filter(School::hasClasses)
                          .collect(Collectors.toList());

                  if (schools.isEmpty())
                  {
                        EmbedUtils.error(event, "**%s** has no schools with classes", event.getGuild().getName());
                        return;
                  }

                  else if (schools.size() == 1)
                  {
                        var school = schools.get(0);
                        var list = school.getClassroomList();

                        if (list.isEmpty())
                        {
                              event.sendMessage("There are currently no classes to list that are in the future");
                              return;
                        }
                        event.sendAsPaginatorWithPageNumbers(list);
                  }

                  else
                  {
                        event.sendAsPaginatorWithPageNumbers(schools);
                        var eventWaiter = event.getSchoolbot().getEventWaiter();

                        eventWaiter.waitForEvent(MessageReceivedEvent.class,
                                messageReceivedEvent -> messageReceivedEvent.getMessageIdLong() == event.getMessage().getIdLong()
                                                        && messageReceivedEvent.getAuthor().getIdLong() == event.getMember().getIdLong()
                                                        && messageReceivedEvent.getMessage().getContentRaw().chars().allMatch(Character::isDigit)
                                                        && Checks.between(Integer.parseInt(messageReceivedEvent.getMessage().getContentRaw()), schools.size()),

                                messageReceivedEventAction ->
                                {
                                      var index = Integer.parseInt(messageReceivedEventAction.getMessage().getContentRaw()) - 1;
                                      var school = schools.get(index);

                                      var list = school.getClassroomList();

                                      if (list.isEmpty())
                                      {
                                            event.sendMessage("There are currently no classes to list that are in the future");
                                            return;
                                      }
                                      event.sendAsPaginatorWithPageNumbers(list);
                                }
                        );
                  }
            }


            if (args.size() == 1)
            {
                  String firstArg = args.get(0);

                  if (!event.schoolExist(firstArg))
                  {
                        EmbedUtils.error(event, "** %s ** does not exist", firstArg);
                        return;
                  }

                  School school = event.getSchool(firstArg);

                  if (school.getClassroomList().isEmpty())
                  {
                        EmbedUtils.error(event, "** %s ** has no classes", school.getName());
                        return;
                  }

                  event.sendAsPaginatorWithPageNumbers(school.getClassroomList());
            }
      }
}
