package io.jenkins.plugins.loadmance.model;

public class TestRunResponse {

  private TestRunStatusDto testRun;

  public TestRunStatusDto getTestRun() {
    return testRun;
  }

  public void setTestRun(TestRunStatusDto testRun) {
    this.testRun = testRun;
  }
}
