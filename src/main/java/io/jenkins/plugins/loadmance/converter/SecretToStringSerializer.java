package io.jenkins.plugins.loadmance.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import hudson.util.Secret;
import java.io.IOException;

public class SecretToStringSerializer extends JsonSerializer<Secret> {

  @Override
  public void serialize(Secret secret, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeObject(secret.getPlainText());
  }
}
