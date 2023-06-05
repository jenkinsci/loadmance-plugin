package io.jenkins.plugins.loadmancetestbuilder.utils;

import io.jenkins.plugins.loadmancetestbuilder.exception.LoadmanceException;
import io.jenkins.plugins.loadmancetestbuilder.model.AuthRequestDto;
import io.jenkins.plugins.loadmancetestbuilder.model.LoginResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

  @Test
  public void testJsonToObject() throws LoadmanceException {
    var modelJsonData = "{\"token\" :\"dadsa\",\"expiresIn\": \"das\"}";
    LoginResponseDto expected = new LoginResponseDto();
    expected.setToken("dadsa");
    LoginResponseDto actual = JsonUtil.jsonToObject(modelJsonData, LoginResponseDto.class);
    Assertions.assertTrue(actual != null);
    Assertions.assertEquals(LoginResponseDto.class, actual.getClass());
    Assertions.assertTrue(actual.getToken().equals(expected.getToken()));
  }

  @Test
  public void testObjectToJson() throws LoadmanceException {
    var modelJsonData = "{\"username\" :\"lm\",\"password\":\"mn\"}";
    AuthRequestDto expected = new AuthRequestDto();
    expected.setUsername("lm");
    expected.setPassword("mn");
    String actual = JsonUtil.objectToJson(modelJsonData);
    Assertions.assertTrue(actual.contains("lm"));
  }

}