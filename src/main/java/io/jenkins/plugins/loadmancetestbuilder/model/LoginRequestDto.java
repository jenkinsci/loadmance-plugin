package io.jenkins.plugins.loadmancetestbuilder.model;

import hudson.util.Secret;
import java.io.Serializable;

public class LoginRequestDto implements Serializable {

  private String username;
  private Secret password;

  public LoginRequestDto(String username, Secret password) {
    this.username = username;
    this.password = password;
  }

  public LoginRequestDto() {
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
