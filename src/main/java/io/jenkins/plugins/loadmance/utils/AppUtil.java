package io.jenkins.plugins.loadmance.utils;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AppUtil {
  INSTANCE;

  private final Logger LOGGER = LoggerFactory.getLogger(AppUtil.class);

  private String baseUrl;

  AppUtil() {
    Properties properties = new Properties();
    try {
      properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
      baseUrl = properties.getProperty("baseUrl");
      LOGGER.debug("app.properties load complete");
    } catch (IOException e) {
      LOGGER.error("Load app.properties error", e);
    }
  }

  public String getBaseUrl() {
    return baseUrl;
  }
}
