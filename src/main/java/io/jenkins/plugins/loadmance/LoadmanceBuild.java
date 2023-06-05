package io.jenkins.plugins.loadmance;

import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.Callable;
import io.jenkins.plugins.loadmance.model.AuthRequestDto;
import io.jenkins.plugins.loadmance.service.LoadmanceService;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;

public class LoadmanceBuild implements Callable<Result, Exception> {

  private static final int TEST_RUN_CHECK_DELAY = 10000;
  private String testId;
  private BuildListener listener = null;

  private AuthRequestDto authRequestDto;

  LoadmanceService loadmanceService = LoadmanceService.INSTANCE;

  @Override
  public Result call() throws Exception {
    String testRunId = null;
    Result result;

    if (StringUtils.isBlank(getTestId())) {
      listener.getLogger().println("Test builder id is not found");
      result = Result.FAILURE;
      return result;
    }

    try {

      loadmanceService.setAuthRequestDto(authRequestDto);

      testRunId = loadmanceService.startTest(testId).getRunId();

      listener.getLogger().println("Start test with run id = " + testRunId);

      while (true) {
        Thread.sleep(TEST_RUN_CHECK_DELAY);

        var testStatus = loadmanceService.getTestRunStatus(testRunId);

        if (testStatus != null && testStatus.getStatus().equalsIgnoreCase("FINISH")) {
          listener.getLogger().printf("%s test finish with status%n", testRunId, testStatus.getResultStatus());
          if (testStatus.getResultStatus().equalsIgnoreCase("PASS")) {
            result = Result.SUCCESS;
          } else {
            result = Result.FAILURE;
          }
          break;
        } else {
          listener.getLogger().printf("%s test continue. Current status=%s%n", testRunId, testStatus.getStatus());
        }

        if (Thread.interrupted()) {
          loadmanceService.stopTest(testRunId);
          throw new InterruptedException("Job stoped by jenkins");
        }
      }


    } catch (InterruptedException e) {
      listener.getLogger().printf("Stop by jenkins");
      throw new InterruptedException("Stop by jenkins");
    } catch (Exception e) {
      result = Result.NOT_BUILT;
      listener.getLogger().printf("Stop by exception with %s", e.getMessage());
    } finally {
      if (!StringUtils.isBlank(testRunId)) {
        loadmanceService.stopTest(testRunId);
      }
    }

    return result;
  }

  @Override
  public void checkRoles(RoleChecker checker) throws SecurityException {

  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public void setListener(BuildListener listener) {
    this.listener = listener;
  }

  public void setAuthRequestDto(AuthRequestDto authRequestDto) {
    this.authRequestDto = authRequestDto;
  }
}
