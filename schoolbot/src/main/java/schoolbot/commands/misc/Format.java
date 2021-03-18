package schoolbot.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import schoolbot.SchoolbotConstants;
import schoolbot.natives.objects.command.Command;
import schoolbot.natives.objects.command.CommandEvent;

public class Format extends Command
{
    public Format()
    {
        super("Visual representation on how to format code using discord", "[none]", 0);
        addCalls("format", "wrap");
    }    

    @Override
    public void run(CommandEvent event) 
    {
        /*
        event.sendMessage(new EmbedBuilder()
        .setDescription("""
                        Surrond code with:
                            \\`\\`\\` language 
                        Person p = new Person(); \\`\\`\\` 
                        This should display: 
                        ```java
                        Person p = new Person(); ``` 
                        Replace 'language' with the alphabetic character (in lower case) of another language. For example: C++ -> cpp, Python -> python or  py 
                        
                        This character can be found at the top left of your keyboard!
                        """)
        .setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR));
        *
         */

        event.sendMessage(new EmbedBuilder()
                            .setDescription("Surrond code with: \n \\`\\`\\` language " +
                                            "\n Person p = new Person(); \\`\\`\\` " +
                                            "\n This should display: "+
                                            "\n ```java\n Person p = new Person(); ``` " +
                                            "\n Replace 'language' with the alphabetic character (in lower case) of another language. For example: C++ -> cpp, Python -> python or  py " +
                                            "\n\n This character can be found at the top left of your keyboard")

                            .setColor(SchoolbotConstants.DEFAULT_EMBED_COLOR));

    }
}
