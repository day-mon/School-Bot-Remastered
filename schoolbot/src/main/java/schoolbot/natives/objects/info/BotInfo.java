package schoolbot.natives.objects.info;

import net.dv8tion.jda.api.JDAInfo;
import schoolbot.SchoolbotConstants;

public class BotInfo
{
     public BotInfo(){};

     public static String getJavaVersion()
     {
         return System.getProperty("java.version");
     }

     public static String getGithubRepo()
     {
         return "https://github.com/tykoooo/School-Bot-Remastered";
     }

     public static String getSchoolbotVersion()
     {
         return SchoolbotConstants.VERSION;
     }

}
