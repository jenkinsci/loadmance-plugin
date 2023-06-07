package io.jenkins.plugins.loadmance.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hudson.util.Secret;
import io.jenkins.plugins.loadmance.converter.SecretToStringSerializer;

public class LoginResponseDto {

  @JsonSerialize(using = SecretToStringSerializer.class)
  private Secret token;

  public LoginResponseDto() {
  }

  public Secret getToken() {
    return token;
  }

  public void setToken(Secret token) {
    this.token = token;
  }
}
