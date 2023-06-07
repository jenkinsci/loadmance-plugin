package io.jenkins.plugins.loadmance.utils;

import hudson.util.Secret;
import io.jenkins.plugins.loadmance.exception.LoadmanceException;
import io.jenkins.plugins.loadmance.model.LoginRequestDto;
import io.jenkins.plugins.loadmance.model.LoginResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

  @Test
  public void testJsonToObject() throws LoadmanceException {
    var modelJsonData = "{\"token\" :\"dadsa\",\"expiresIn\": \"das\"}";
    LoginResponseDto expected = new LoginResponseDto();
    expected.setToken(Secret.fromString("dadsa"));
    LoginResponseDto actual = JsonUtil.jsonToObject(modelJsonData, LoginResponseDto.class);
    Assertions.assertTrue(actual != null);
    Assertions.assertEquals(LoginResponseDto.class, actual.getClass());
    Assertions.assertTrue(actual.getToken().getPlainText().equals(expected.getToken().getPlainText()));
  }

  @Test
  public void testObjectToJson() throws LoadmanceException {
    var modelJsonData = "{\"username\" :\"lm\",\"password\":\"mn\"}";
    LoginRequestDto expected = new LoginRequestDto("lm", Secret.decrypt("mn"));
    String actual = JsonUtil.objectToJson(modelJsonData);
    Assertions.assertTrue(actual.contains("lm"));
  }

  @Test
  public void testSecret() throws LoadmanceException {
    var secret= Secret.fromString("km");
    Assertions.assertEquals("km",secret.getPlainText());
  }

  @Test
  public void testSecretDeserialize() throws LoadmanceException {
    var secret= Secret.fromString("km");
    var loginRequest =new LoginRequestDto("lm",secret);
    var payload =JsonUtil.objectToJson(loginRequest);
    Assertions.assertTrue(payload.contains("km"));
    Assertions.assertTrue(payload.contains("lm"));
  }



}