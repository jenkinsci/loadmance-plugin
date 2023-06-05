package io.jenkins.plugins.loadmance.model;

import java.util.List;

public class ProjectListResponse {
  private List<ProjectDto> items;

  public ProjectListResponse() {
  }

  public List<ProjectDto> getItems() {
    return items;
  }

  public void setItems(List<ProjectDto> items) {
    this.items = items;
  }
}
