package schoolbot.util;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import schoolbot.objects.misc.StateMachineValues;
import schoolbot.objects.misc.interfaces.Paginatable;
import schoolbot.objects.school.Classroom;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Processor
{
      private Processor()
      {
      }


      /**
       * Returns void
       * The event parameter is just so I can grab the schoolbot object and the channel
       * The professor list is the list being passed through for processing so we can check certain conditions
       *
       * @param values      Values passed through that majority of state machine use
       * @param genericList List of objects sent in for processing
       */
      public static <T extends Paginatable> int processGenericList(StateMachineValues values, List<T> genericList, Class<?> tClass)
      {
            var event = values.getCommandEvent();
            int size = genericList.size();
            var schoolbot = event.getSchoolbot();
            var channel = values.getCommandEvent().getChannel();

            if (genericList.isEmpty())
            {
                  EmbedUtils.error(event, processErrorMessage(tClass, values));
                  return 0;
            }
            else if (size == 1)
            {
                  T object = genericList.get(0);
                  values.setValue(object);
                  channel.sendMessageEmbeds(object.getAsEmbed(schoolbot))
                          .append("This embed will be removed in 20 seconds to reduce clutter")
                          .queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
                  return 1;
            }
            else
            {
                  event.sendAsPaginatorWithPageNumbers(genericList);
                  values.setList(genericList);
                  event.sendSelfDeletingMessageFormat("Choose a page number from the list of %s's.", tClass.getSimpleName());
                  return 2;
            }
      }

      /**
       * Returns void
       * The event parameter is just so I can grab the schoolbot object and the channel
       * The professor list is the list being passed through for processing so we can check certain conditions
       *
       * @param values      Values passed through that majority of state machine use
       * @param genericList List of objects sent in for processing
       */
      public static <T extends Paginatable> int processGenericListWithSendingList(StateMachineValues values, List<T> genericList, Class<?> tClass)
      {
            var event = values.getCommandEvent();

            int size = genericList.size();

            if (genericList.isEmpty())
            {
                  EmbedUtils.error(event, processErrorMessage(tClass, values));
                  return 0;
            }
            else if (size == 1)
            {
                  T object = genericList.get(0);
                  values.setValue(object);
                  return 1;
            }
            else
            {
                  values.setList(genericList);
                  event.sendAsPaginatorWithPageNumbers(genericList);
                  event.sendSelfDeletingMessageFormat("Choose a page number from the list of %s's.", tClass.getSimpleName());
                  return 2;
            }
      }

      /**
       * Returns void
       * The event parameter is just so I can grab the schoolbot object and the channel
       * The professor list is the list being passed through for processing so we can check certain conditions
       *
       * @param values      Values passed through that majority of state machine use
       * @param genericList List of objects sent in for processing
       */
      public static <T extends Paginatable> int processGenericListWithoutMessageSend(StateMachineValues values, List<T> genericList, Class<?> tClass)
      {
            var event = values.getCommandEvent();

            int size = genericList.size();

            if (genericList.isEmpty())
            {
                  EmbedUtils.error(event, processErrorMessage(tClass, values));
                  return 0;
            }
            else if (size == 1)
            {
                  T object = genericList.get(0);
                  values.setValue(object);
                  return 1;
            }
            else
            {
                  event.sendAsPaginatorWithPageNumbers(genericList);
                  values.setList(genericList);
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
                  EmbedUtils.notANumberError(event, message);
                  return null;
            }

            int pageNumber = Integer.parseInt(message);

            if (!Checks.between(pageNumber, genericList.size()))
            {
                  EmbedUtils.error(event, "** %s ** was not a valid entry. Please retry with a valid entry!", message);
                  return null;
            }

            return genericList.get(pageNumber - 1);
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
       * <p>
       * At the end of the method the state will be incremented and the value will be set if valid.
       *
       * @param values      StateMachineValues.
       * @param genericList List full of objects
       * @param <T>         N/A
       * @return If message is a number and its a valid index method succeeds otherwise fails
       */
      public static <T extends Paginatable> boolean validateMessage(@NotNull StateMachineValues values, @NotNull List<T> genericList)
      {
            var event = values.getMessageReceivedEvent();
            var message = event.getMessage().getContentRaw();

            if (!Checks.isNumber(message))
            {
                  EmbedUtils.notANumberError(event, message);
                  return false;
            }

            int pageNumber = Integer.parseInt(message);

            if (!Checks.between(pageNumber, genericList.size()))
            {
                  EmbedUtils.error(event, "** %s ** was not a valid entry. Please retry with a valid entry!", message);
                  return false;
            }

            var element = genericList.get(pageNumber - 1);

            values.setValue(element);
            values.incrementMachineState();

            return true;
      }


      /**
       * This method returns a list of classes that the user is associated with.
       * <br>
       * The @param values is just the values of the current machine
       *
       * @param values StateMachine values of current machine
       * @return List of Classes that users roles are associated with
       */
      public static List<Classroom> processUserRoles(@NotNull StateMachineValues values)
      {
            var member = values.getCommandEvent().getMember();
            var userRoles = member.getRoles().stream().map(Role::getIdLong).collect(Collectors.toList());

            return values.getCommandEvent().getGuildClasses()
                    .stream()
                    .filter(classroom -> classroom.getRoleID() != 0L)
                    .filter(classroom ->
                    {
                          var roleID = classroom.getRoleID();

                          return userRoles.contains(roleID);
                    }).collect(Collectors.toList());
      }


      private static String processErrorMessage(Class<?> tClass, StateMachineValues values)
      {
            var guild = values.getCommandEvent().getGuild();
            String className = tClass.getSimpleName();

            switch (className)
            {
                  case "School" -> {
                        return "%s has no schools that follow the criteria I am searching for.".formatted(guild.getName());
                  }

                  case "Classroom" -> {
                        var school = values.getSchool();
                        return "** %s ** has no classes".formatted(school.getName());
                  }

                  case "Professor" -> {
                        var school = values.getSchool();
                        return "** %s ** has no professors that meet the criteria in which I am searching for.".formatted(school.getName());
                  }

                  case "Assignment" -> {
                        var classroom = values.getClassroom();
                        return "** %s ** has no assignments".formatted(classroom.getName());
                  }

                  default -> throw new IllegalStateException("You have used a class that does not extend Paginatable.. Please look around and see whats going on");
            }
      }
}
