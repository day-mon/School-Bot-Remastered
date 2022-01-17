package schoolbot.objects.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.Set;

public record Config(String token, DatabaseConfig databaseConfig, Set<Long> developerIds)
{

      public Config()
      {
            this("token", new DatabaseConfig(), Set.of(1L, 2L));
      }

      @JsonIgnore
      public String getAsJson()
      {
            var mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            try
            {
                  return mapper.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                  return  "";
            }
      }
}