package schoolbot.natives.objects.config;

public enum ConfigOption
{
      TOKEN("token", "token"),
      DBUSER("DBUSERNAME", "username"),
      DBPASSWORD("DBPASSWORD", "password"),
      DBHOSTNAME("DBHOSTANME", "HOSTNAME"),
      JDBCURL("JDBCURL", "URL");


      private String key;
      private String defaultValue;

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
