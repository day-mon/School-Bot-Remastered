package schoolbot.commands.school;

import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.Page;
import com.github.ygimenez.type.PageType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;
import schoolbot.natives.objects.school.Classroom;
import schoolbot.natives.objects.school.School;
import schoolbot.natives.util.Embed;

import java.util.ArrayList;

public class ListClasses extends Command
{
      public ListClasses()
      {
            super("", "", 1);
            addCalls("classes", "classlist");
      }


      /**
       * What the command will do on call.
       *
       * @param event Arguments sent to the command.
       */
      @Override
      public void run(CommandEvent event)
      {
            String firstArg = event.getArgs().get(0);

            if (!event.schoolExist(firstArg))
            {
                  Embed.error(event, "** %s ** does not exist", firstArg);
                  return;
            }

            School school = event.getSchool(event, firstArg);

            if (school.getClassesSize() < 0)
            {
                  Embed.error(event, "** %s ** has no classes", school.getSchoolName());
                  return;
            }

            ArrayList<Page> pages = new ArrayList<>();

            for (Classroom classroom : school.getClassroomList())
            {
                  pages.add(new Page(PageType.EMBED, classroom.getAsEmbedShort(event.getSchoolbot())));
            }

            event.getChannel().sendMessage((MessageEmbed) pages.get(0).getContent())
                    .queue(success -> Pages.paginate(success, pages));
      }
}
