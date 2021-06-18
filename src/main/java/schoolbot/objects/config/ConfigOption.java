package schoolbot.objects.config;

public enum ConfigOption
{
      TOKEN("token", "token"),
      DBUSER("DBUSERNAME", "username"),
      DBPASSWORD("DBPASSWORD", "password"),
      DBHOSTNAME("DBHOSTANME", "HOSTNAME"),
      JDBCURL("JDBCURL", "URL"),
      DBDRIVER("DBDRIVER", "DRIVER");


      private final String key;
      private final String defaultValue;

      ConfigOption(String key, String defaultValue)
      {
            this.key = key;
            this.defaultValue = defaultValue;
      }

      public String getDefaultValue()
      {
            return defaultValue;
      }

      public String getKey()
      {
            return key;
      }
}
