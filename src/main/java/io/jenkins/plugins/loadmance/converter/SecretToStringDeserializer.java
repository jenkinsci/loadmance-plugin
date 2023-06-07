package io.jenkins.plugins.loadmance.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import hudson.util.Secret;
import java.io.IOException;

public class SecretToStringDeserializer extends JsonDeserializer<Secret> {

  @Override
  public Secret deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    return Secret.fromString(jsonParser.getValueAsString());
  }
}
