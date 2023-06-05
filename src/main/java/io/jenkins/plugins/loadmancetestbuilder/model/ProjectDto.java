package io.jenkins.plugins.loadmancetestbuilder.model;

public class ProjectDto {

  private String id;
  private String title;

  public ProjectDto() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
