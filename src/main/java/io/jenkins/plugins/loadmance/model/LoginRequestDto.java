package io.jenkins.plugins.loadmance.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import hudson.util.Secret;
import io.jenkins.plugins.loadmance.converter.SecretToStringSerializer;
import java.io.Serializable;

public class LoginRequestDto implements Serializable {

  private String username;

  @JsonSerialize(using = SecretToStringSerializer.class)
  private Secret password;

  public LoginRequestDto(String username, Secret password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Secret getPassword() {
    return password;
  }

  public void setPassword(Secret password) {
    this.password = password;
  }
}

