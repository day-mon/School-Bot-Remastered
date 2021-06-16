package schoolbot.util;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import schoolbot.objects.command.CommandEvent;
import schoolbot.objects.misc.Paginatable;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.school.Classroom;

import java.util.List;

public class Processor
{
      /**
       * Returns the next state the state machine should goto
       * The Event field is just so I can use the channel
       * The Classroom List is the list that needs processing
       *
       * @param event         Event Passed through for channel
       * @param classroomList Class List passed through for processing
       * @return Returns state to go to
       */
      public static int processClassList(CommandEvent event, List<Classroom> classroomList, String UPDATE_MENU)
      {

            if (classroomList.size() == 1)
            {
                  Classroom classroom = classroomList.get(0);

                  event.sendMessage(UPDATE_MENU, classroom.getName());
                  return 3;

            }
            else
            {
                  event.sendAsPaginatorWithPageNumbers(classroomList);
                  return 2;
            }
      }

      /**
       * Returns void
       * The event parameter is just so I can grab the schoolbot object and the channel
       * The professor list is the list being passed through for processing so we can check certain conditions
       *
       * @param values      Values passed through that majarity of state machine use
       * @param genericList List of objects sent in for processing
       */
      public static <T extends Paginatable> int processGenericList(StateMachineValues values, List<T> genericList, Class<?> tClass)
      {
            var event = values.getEvent();

            int size = genericList.size();
            var channel = event.getChannel();
            var schoolbot = event.getSchoolbot();

            if (genericList.isEmpty())
            {
                  Embed.error(event, processErrorMessage(tClass, values));
                  return 0;
            }
            else if (size == 1)
            {
                  T object = genericList.get(0);
                  values.setValue(object);
                  event.sendMessage(object.getAsEmbed(schoolbot));
                  return 1;
            }
            else
            {
                  event.sendAsPaginatorWithPageNumbers(genericList);
                  values.setList(genericList);
                  event.sendMessage("Choose a page number from the list of %s's.", tClass.getSimpleName());
                  return 2;
            }
      }


      /**
       * Nullable method
       * <br>
       * Returns null if there was some sort of error, if not it will return object in respect to the page
       * Method Checks the following
       *
       * <ul>
       * <li> If a number is the message that is sent  </li>
       * <li> Checks if that number is a valid index from the list of options the user has to choose </li>
       * </ul>
       *
       * @param event       Event Validation
       * @param genericList List full of objects
       * @param <T>         N/A
       * @return If message is a number and its a valid index method succeeds otherwise fails
       */
      @Nullable
      public static <T> T validateMessage(@NotNull GuildMessageReceivedEvent event, @NotNull List<T> genericList)
      {
            var message = event.getMessage().getContentRaw();

            if (!Checks.isNumber(message))
            {
                  Embed.notANumberError(event, message);
                  return null;
            }

            int pageNumber = Integer.parseInt(message);

            if (!Checks.between(pageNumber, genericList.size()))
            {
                  Embed.error(event, "** %s ** was not a valid entry. Please retry with a valid entry!", message);
                  return null;
            }

            return genericList.get(pageNumber - 1);
      }

      private static String processErrorMessage(Class<?> tClass, StateMachineValues values)
      {
            var guild = values.getEvent().getGuild();
            String className = tClass.getSimpleName();

            switch (className)
            {
                  case "School" -> {
                        return "%s has no schools that following the criteria I am searching for.".formatted(guild.getName());
                  }

                  case "Classroom" -> {
                        var school = values.getSchool();
                        return "** %s ** has no classes".formatted(school.getName());
                  }

                  case "Professor" -> {
                        var school = values.getSchool();
                        return "** %s ** has no professors".formatted(school.getName());
                  }

                  case "Assignment" -> {
                        var classroom = values.getClassroom();
                        return "** %s ** has no assignments".formatted(classroom.getName());
                  }

                  default -> throw new IllegalStateException("You have used a class that does not extend Paginatable.. Please look around and see whats going on");
            }
      }
}