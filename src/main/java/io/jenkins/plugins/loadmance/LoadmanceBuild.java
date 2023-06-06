package io.jenkins.plugins.loadmance;

import hudson.model.BuildListener;
import hudson.model.Result;
import io.jenkins.plugins.loadmance.model.LoginRequestDto;
import io.jenkins.plugins.loadmance.service.LoadmanceService;
import jenkins.security.NotReallyRoleSensitiveCallable;
import org.apache.commons.lang.StringUtils;

public class LoadmanceBuild extends NotReallyRoleSensitiveCallable<Result, Exception> {

  private static final int TEST_RUN_CHECK_DELAY = 10000;
  private String testId;
  private BuildListener listener = null;

  private LoginRequestDto loginRequestDto;

  @Override
  public Result call() throws Exception {
    String testRunId = null;
    Result result;

    if (StringUtils.isBlank(getTestId())) {
      listener.getLogger().println("Test builder id is not found");
      result = Result.FAILURE;
      return result;
    }

    LoadmanceService loadmanceService = LoadmanceService.getInstance();
    try {
      loadmanceService.updateLoginRequestDto(loginRequestDto);

      testRunId = loadmanceService.startTest(testId).getRunId();

      listener.getLogger().println("Start test with run id = " + testRunId);

      while (true) {
        Thread.sleep(TEST_RUN_CHECK_DELAY);

        var testStatus = loadmanceService.getTestRunStatus(testRunId);

        if (testStatus == null) {
          continue;
        }

        if (testStatus.getStatus().equalsIgnoreCase("FINISH")) {
          listener.getLogger().printf("%s test finish with status %s n", testRunId, testStatus.getResultStatus());
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
      listener.getLogger().println("Stop by jenkins");
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


  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  public void setListener(BuildListener listener) {
    this.listener = listener;
  }

  public void setLoginRequestDto(LoginRequestDto loginRequestDto) {
    this.loginRequestDto = loginRequestDto;
  }
}
