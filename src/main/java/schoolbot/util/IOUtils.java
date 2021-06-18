package schoolbot.util;

import java.net.URISyntaxException;
import java.nio.file.Path;

public class IOUtils
{

      public static String getFileExtension(Path path)
      {
            return path.toString().substring(path.toString().lastIndexOf(".") + 1);
      }

      public static Path getJarPath(Class<?> classs)
      {
            try
            {
                  return Path.of(classs.getProtectionDomain().getCodeSource().getLocation().toURI());
            }
            catch (URISyntaxException e)
            {
                  throw new RuntimeException(e);
            }
      }

}
