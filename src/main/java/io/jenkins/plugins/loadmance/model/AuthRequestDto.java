package io.jenkins.plugins.loadmance.model;

public class AuthRequestDto {
  private String username;
  private String password;

  public AuthRequestDto(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public AuthRequestDto() {
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
