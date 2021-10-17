package schoolbot.commands.school;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;
import schoolbot.objects.command.Command;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.command.CommandFlag;
import schoolbot.objects.misc.Reminder;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.Assignment;
import schoolbot.objects.school.Classroom;
import schoolbot.util.Checks;
import schoolbot.util.DatabaseUtils;
import schoolbot.util.EmbedUtils;
import schoolbot.util.Processor;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReminderView extends Command
{

      public ReminderView(Command parent)
      {
            super(parent, "Views a list of Reminders given a date", "[date]", 1);
            addFlags(CommandFlag.STATE_MACHINE_COMMAND);
            addUsageExample("reminder view 9/13/2021");
            addCommandPrerequisites("Has to be in a class channel");
      }

      @Override
      public void run(@NotNull CommandEvent event, @NotNull List<String> args, @NotNull StateMachineValues values)
      {
            var classroom = Checks.messageSentFromClassChannel(values);

            if (classroom == null)
            {
                  EmbedUtils.error(event, "You did not send this message from a channel that is associated with a classroom");
                  return;
            }

            var potentialDate = Checks.checkValidDate(args.get(0));

            if (potentialDate == null)
            {
                  EmbedUtils.error(event, "Could not parse date. Format `M/d/yyyy`");
                  return;
            }

            if (classroom.hasAssignments())
            {
                  var selectionOptions = List.of(
                          SelectOption.of("Assignment", "assignment"),
                          SelectOption.of("Class", "class")
                  );

                  event.sendMenuAndAwait("View for assignments or class", selectionOptions, (buttonEvent) ->
                  {
                        var option = buttonEvent.getValues().get(0);

                        if (option.equals("assignment"))
                        {
                              var finalValue = Processor.processGenericList(values, classroom.getAssignments(), Assignment.class);

                              if (finalValue == 1)
                              {
                                    var assignment = values.getAssignment();
                                    var list = DatabaseUtils.getRemindersOnDate(event, assignment, potentialDate);

                                    if (list == null)
                                    {
                                          EmbedUtils.error(event, "Error occurred while attempting to retrieve reminders");
                                          return;
                                    }


                                    var subList = generateEmbed(list, option);

                                    event.bPaginator(subList);

                              }
                        }
                        else if (option.equals("class"))
                        {
                              var list = DatabaseUtils.getRemindersOnDate(event, values.getClassroom(), potentialDate);

                              if (list == null)
                              {
                                    EmbedUtils.error(event, "Error occurred while attempting to retrieve reminders");
                                    return;
                              }

                              var subList = generateEmbed(list, "classroom");

                              event.bPaginator(subList);
                        }
                  });
            }


            // clarify because its so far down :)
            else if (!classroom.hasAssignments())
            {
                  var list = DatabaseUtils.getRemindersOnDate(event, values.getClassroom(), potentialDate);

                  if (list == null)
                  {
                        EmbedUtils.error(event, "Error occurred while attempting to retrieve reminders");
                        return;
                  }

                  if (list.isEmpty())
                  {
                        EmbedUtils.error(event, "There are no reminders for the date: %s", potentialDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                        return;
                  }

                  var subList = generateEmbed(list, "classroom");


                  event.bPaginator(subList);
            }
      }

      private List<MessageEmbed> generateEmbed(List<Reminder> list, String choice)
      {
            if (choice.equalsIgnoreCase("classroom"))
            {

                  return list.stream()
                          .map(item ->
                          {
                                var classroom = (Classroom) item.obj();
                                var time = item.time()[0];
                                var delta = Duration.between(time.toLocalTime(), classroom.getStartDateWithTime().toLocalTime()).toMinutes();
                                return new EmbedBuilder()
                                        .setTitle("Reminder for " + classroom.getName() + " | ID (#" + item.id() + ")")
                                        .addField("Remind Time", time.format(DateTimeFormatter.ofPattern("MM-dd-yyy @ HH:mm")) + " | ** " + delta + "** (minutes before)", false)
                                        .addField("Class Start Time", classroom.getStartDateWithTime().format(DateTimeFormatter.ofPattern("MM-dd-yyy @ HH:mm")), false)
                                        .build();
                          })
                          .collect(Collectors.toList());
            }
            else
            {
                  return list.stream()
                    .map(item ->
                    {
                          var assignment1 = (Assignment) item.obj();
                          return new EmbedBuilder()
                                  .setTitle("Reminder for " + assignment1.getName() + " ID (#" + item.id() + ")")
                                  .addField("Remind Time", String.valueOf(item.time()), false)
                                  .addField("Due Date", String.valueOf(assignment1.getDueDate()), false)
                                  .build();
                    })
                    .collect(Collectors.toList());
            }
      }

}
