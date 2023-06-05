package io.jenkins.plugins.loadmancetestbuilder.model;

public class TestRunResponse {

  private TestRunStatusDto testRun;

  public TestRunStatusDto getTestRun() {
    return testRun;
  }

  public void setTestRun(TestRunStatusDto testRun) {
    this.testRun = testRun;
  }
}
