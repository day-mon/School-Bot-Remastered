package schoolbot.objects.config;


public record DatabaseConfig(String dbUser, String dbPassword, String dbDriver, String dbHostName, String jbdcUrl)
{
      public DatabaseConfig()
      {
            this("DB-USER", "DB-PASSWORD", "DB-DRIVER", "DB-HOSTNAME", "JBDC-URL");
      }
}

