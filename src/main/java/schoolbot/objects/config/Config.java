package schoolbot.objects.config;

import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.Set;

public class Config
{
      private String token = "TOKEN";
      private String dbUser = "DB-USER";
      private String dbPassword = "DB-PASSWORD";
      private String dbDriver = "DB-DRIVER";
      private String dbHostName = "DB-HOSTNAME";
      private String jdbcUrl = "URL";
      private Set<Long> developerIds = Set.of(-1L, -2L);

      public Config()
      {

      }


      public String getToken()
      {
            return token;
      }

      public void setToken(String token)
      {
            this.token = token;
      }

      public String getDbUser()
      {
            return dbUser;
      }

      public void setDbUser(String dbUser)
      {
            this.dbUser = dbUser;
      }

      public String getDbPassword()
      {
            return dbPassword;
      }

      public void setDbPassword(String dbPassword)
      {
            this.dbPassword = dbPassword;
      }

      public String getDbHostName()
      {
            return dbHostName;
      }

      public void setDbHostName(String dbHostName)
      {
            this.dbHostName = dbHostName;
      }

      public String getJdbcUrl()
      {
            return jdbcUrl;
      }

      public void setJbcUrl(String jbcUrl)
      {
            this.jdbcUrl = jbcUrl;
      }

      public Set<Long> getDeveloperIds()
      {
            return developerIds;
      }

      public void setDeveloperIds(Set<Long> developerIds)
      {
            this.developerIds = developerIds;
      }

      public String getDbDriver()
      {
            return dbDriver;
      }

      public void setDbDriver(String dbDriver)
      {
            this.dbDriver = dbDriver;
      }

      public String getAsJson()
      {
            return DataObject.empty()
                    .put("token", this.token)
                    .put("dbUser", this.dbUser)
                    .put("dbPassword", this.dbPassword)
                    .put("dbDriver", this.dbDriver)
                    .put("dbHostName", this.dbHostName)
                    .put("jdbcUrl", this.jdbcUrl)
                    .put("developerIds", this.developerIds)
                    .toPrettyString();
      }
}
