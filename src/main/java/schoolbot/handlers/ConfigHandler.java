package schoolbot.handlers;

import schoolbot.Schoolbot;
import schoolbot.objects.config.ConfigOption;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigHandler
{
      public static final File CONFIG_FOLDER = new File("config");
      public static final File CONFIG_FILE = new File(CONFIG_FOLDER, "schoolbot.cfg");
      private final List<ConfigurationValue> configValues;
      private final Schoolbot schoolbot;


      public ConfigHandler(Schoolbot schoolbot)
      {
            initFolder();
            initFile();
            this.configValues = loadValues();
            this.schoolbot = schoolbot;
      }

      private void initFolder()
      {
            try
            {
                  CONFIG_FOLDER.mkdir();
            }
            catch (Exception e)
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
            catch (Exception e)
            {
                  schoolbot.getLogger().error("CONFIG FILE COULD NOT BE CREATED");
                  System.exit(1);
            }
      }

      private List<ConfigurationValue> loadDefaults(List<ConfigurationValue> configValues)
      {
            for (ConfigOption configOption : ConfigOption.values())
            {
                  if (configValues.stream().map(ConfigurationValue::getKey).noneMatch(key -> configOption.getKey().equals(key)))
                  {
                        configValues.add(new ConfigurationValue(configOption.getKey(), configOption.getDefaultValue()));
                  }
            }
            save(configValues);
            return configValues;

      }

      private List<ConfigurationValue> loadValues()
      {
            try
            {
                  List<ConfigurationValue> values = new ArrayList<>();
                  BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE));
                  String line;
                  while ((line = br.readLine()) != null)
                  {
                        /**
                         * Read values;
                         */
                        if (!line.contains("=") || line.startsWith("*"))
                        {
                              /**
                               * Not a valid config lone
                               */
                              continue;
                        }
                        /**
                         * splits the string by the equal side
                         * Ex:
                         * TESTKEY=TESTVALUE
                         * elementSplit[0]=TESTKEY;
                         * elementSplit[1]=TESTVALUE;
                         *
                         */
                        String[] elementSplit = line.split("=");
                        values.add(new ConfigurationValue(elementSplit[0], elementSplit[1]));
                  }
                  return loadDefaults(values);
            }
            catch (Exception e)
            {
                  System.err.print("Something went wrong!");
                  e.printStackTrace();
                  return Collections.emptyList();
            }
      }

      public String getString(ConfigOption configOption)
      {
            /**
             * Thread safety, dont be silly, dont mess with jda P_P
             */
            synchronized (configValues)
            {
                  for (ConfigurationValue configurationValue : configValues)
                  {
                        if (configurationValue.key.equals(configOption.getKey()))
                        {
                              return configurationValue.getValue();
                        }
                  }
                  return configOption.getDefaultValue();
            }
      }

      private void save(List<ConfigurationValue> configValues)
      {
            StringBuilder stringBuilder = new StringBuilder();
            for (ConfigurationValue configurationValue : configValues)
            {
                  stringBuilder
                          .append(configurationValue.key)
                          .append("=")
                          .append(configurationValue.value)
                          .append("\n");
            }
            try
            {
                  FileWriter fileWriter = new FileWriter(CONFIG_FILE);
                  fileWriter.write(stringBuilder.toString());
                  fileWriter.flush();
            }
            catch (Exception exception)
            {
                  schoolbot.getLogger().error("CONFIG ERROR HAS OCCURED", exception);

                  exception.printStackTrace();
            }
      }


      private static class ConfigurationValue
      {
            private String key;
            private String value;

            public ConfigurationValue(String key, String value)
            {
                  this.key = key;
                  this.value = value;
            }

            public String getKey()
            {
                  return key;
            }

            public void setKey(String key)
            {
                  this.key = key;
            }

            public String getValue()
            {
                  return value;
            }

            public void setValue(String value)
            {
                  this.value = value;
            }
      }

}