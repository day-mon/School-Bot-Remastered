package schoolbot.natives.objects.info;

import schoolbot.SchoolbotConstants;

public class BotInfo
{
     public BotInfo(){};

     public static String getGithubRepo()
     {
         return "https://github.com/tykoooo/School-Bot-Remastered";
     }

     public static String getSchoolbotVersion()
     {
         return SchoolbotConstants.VERSION;
     }

}
