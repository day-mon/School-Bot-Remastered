package schoolbot.objects.info;

public class SystemInfo
{
      public SystemInfo()
      {

      }

      public static String getJavaVersion()
      {
            return System.getProperty("java.version");
      }

      public static String getOperatingSystem()
      {
            return System.getProperty("os.name");
      }

}
