package schoolbot.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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


      /**
       * Returns the root Path of the correct FileSystem, default if in a folder, ZipFileSystem if in a JAR
       *
       * @param clazz Class of the JAR file
       * @return Root Path to the JAR file
       * @throws IOException In FileSystem exception
       */
      public static Path getJarFilesystem(Class<?> clazz) throws IOException
      {
            Path jarPath = IOUtils.getJarPath(clazz);
            if (IOUtils.getFileExtension(jarPath).equals("jar"))
            {
                  final FileSystem zfs = FileSystems.newFileSystem(jarPath, (ClassLoader) null);
                  return zfs.getPath("");
            }
            return jarPath;
      }

}
