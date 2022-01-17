package schoolbot.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schoolbot.Schoolbot;
import schoolbot.objects.config.Config;
import schoolbot.util.Checks;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler
{

      public static final File CONFIG_FOLDER = new File("config");
      public static final File CONFIG_FILE = new File(CONFIG_FOLDER, "schoolbot.cfg");
      private final Config config;
      private final Schoolbot schoolbot;
      private final Logger CONFIG_LOGGER = LoggerFactory.getLogger(this.getClass());

      public ConfigHandler(Schoolbot schoolbot)
      {
            initFolder();
            initFile();
            this.schoolbot = schoolbot;
            this.config = loadValues();

      }

      private void initFolder()
      {
            try
            {
                  CONFIG_FOLDER.mkdir();
            }
            catch (RuntimeException e)
            {
                  schoolbot.getLogger().error("CONFIG FOLDER COULD NOT BE CREATED");
                  System.exit(1);
            }
      }

      private void initFile()
      {
            try
            {
                  CONFIG_FILE.createNewFile();
            }
            catch (IOException e)
            {
                  schoolbot.getLogger().error("CONFIG FILE COULD NOT BE CREATED");
                  System.exit(1);
            }
      }

      private Config loadValues()
      {
            Config config = new Config();
            var om = new ObjectMapper();
            try
            {
                  var json = Files.readString(Path.of(String.valueOf(CONFIG_FILE)));

                  if (!Checks.isValidJson(json) || json.isEmpty())
                  {
                        save(config);
                        return config;
                  }


                  config = om.readValue(
                          json,
                          Config.class
                  );


                  return config;
            }
            catch (JsonProcessingException exception)
            {
                  CONFIG_LOGGER.error("Could not parse config. Loading default values", exception);
                  save(config);
                  return config;
            }
            catch (IOException exception)
            {
                  CONFIG_LOGGER.error("Error occurred while. loading the file", exception);
                  save(config);
                  return config;
            }
      }


      private void save(Config config)
      {
            var json = config.getAsJson();

            if (json.isBlank())
            {
                  CONFIG_LOGGER.error("Error occurred while attempting to process json");
                  return;
            }

            try
            {
                  FileWriter fileWriter = new FileWriter(CONFIG_FILE);
                  fileWriter.write(json);
                  fileWriter.flush();
            }
            catch (IOException exception)
            {
                  schoolbot.getLogger().error("CONFIG ERROR HAS OCCURRED", exception);
            }
      }

      public Config getConfig()
      {
            return config;
      }
}