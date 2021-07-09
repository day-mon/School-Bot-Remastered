package schoolbot.objects.info;

public class SystemInfo
{
      public static String getJavaVersion()
      {
            return String.format("%s by %s", System.getProperty("java.version"), System.getProperty("java.vendor"));
      }

      public static String getOperatingSystem()
      {
            return String.format("%s (%s) on v%s ", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
      }

}
