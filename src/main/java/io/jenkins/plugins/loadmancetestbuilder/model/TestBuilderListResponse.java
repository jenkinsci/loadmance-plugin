package io.jenkins.plugins.loadmancetestbuilder.model;

import java.util.List;

public class TestBuilderListResponse {
  private List<TestBuilderDto> items;

  public TestBuilderListResponse() {
  }

  public List<TestBuilderDto> getItems() {
    return items;
  }

  public void setItems(List<TestBuilderDto> items) {
    this.items = items;
  }
}
