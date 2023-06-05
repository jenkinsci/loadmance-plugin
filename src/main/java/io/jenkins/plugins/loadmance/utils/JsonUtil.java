package io.jenkins.plugins.loadmance.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jenkins.plugins.loadmance.exception.LoadmanceException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
  private static final ObjectMapper objectMapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static <T> String objectToJson(T data) throws LoadmanceException {
    if (data == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      LOGGER.error("Object to json error", e);
      throw new LoadmanceException("Json to object error", e);
    }
  }

  public static <T> T jsonToObject(String data, Class<T> tClass) throws LoadmanceException {
    try {
      return objectMapper.readValue(data, tClass);
    } catch (IOException e) {
      LOGGER.error("Json to object error", e);
      throw new LoadmanceException("Json to object error", e);
    }
  }

  public static <T> T jsonToObject(String data, TypeReference<T> tClass) throws LoadmanceException {
    try {
      return objectMapper.readValue(data, tClass);
    } catch (IOException e) {
      LOGGER.error("Json to object error", e);
      throw new LoadmanceException("Json to object error", e);
    }
  }


}
