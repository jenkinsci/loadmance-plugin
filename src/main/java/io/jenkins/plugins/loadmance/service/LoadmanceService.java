package io.jenkins.plugins.loadmance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import hudson.util.Secret;
import io.jenkins.plugins.loadmance.exception.LoadmanceException;
import io.jenkins.plugins.loadmance.model.LoginRequestDto;
import io.jenkins.plugins.loadmance.model.LoginResponseDto;
import io.jenkins.plugins.loadmance.model.ProjectDto;
import io.jenkins.plugins.loadmance.model.TestBuilderDto;
import io.jenkins.plugins.loadmance.model.TestBuilderListResponse;
import io.jenkins.plugins.loadmance.model.TestRunDto;
import io.jenkins.plugins.loadmance.model.TestRunResponse;
import io.jenkins.plugins.loadmance.model.TestRunStatusDto;
import io.jenkins.plugins.loadmance.utils.AppUtil;
import io.jenkins.plugins.loadmance.utils.JsonUtil;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadmanceService {

  private static LoadmanceService instance;
  private static final Object lock = new Object();

  public static LoadmanceService getInstance() {
    synchronized (lock) {
      if (instance == null) {
        instance = new LoadmanceService();
      }
    }
    return instance;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(LoadmanceService.class);
  private static final HttpClient httpClient = HttpClient.newHttpClient();

  private static final String HEADER_CONTENT_TYPE = "Content-Type";
  private static final String HEADER_CONTENT_TYPE_VALUE = "application/json";
  private static final String HEADER_ACCEPT = "Content-Type";
  private static final String HEADER_ACCEPT_VALUE = "application/json";
  private static final String HEADER_AUTHORIZATION = "Authorization";
  private LoginRequestDto loginRequestDto;

  public void updateLoginRequestDto(LoginRequestDto loginRequestDto) {
    this.loginRequestDto = loginRequestDto;
  }

  public LoginResponseDto login(String username, Secret password) throws LoadmanceException {
    this.loginRequestDto = new LoginRequestDto(username, password);
    return getToken();
  }

  public String getTokenValue() throws LoadmanceException {
    return getToken().getToken().getPlainText();
  }

  public LoginResponseDto getToken() throws LoadmanceException {
    var request = createPostRequest("auth/login", loginRequestDto).build();
    try {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return JsonUtil.jsonToObject(response.body(), LoginResponseDto.class);
      }
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Error client request", e);
      throw new LoadmanceException("Request error", e);
    }
    return null;
  }

  public List<ProjectDto> getProjects() throws LoadmanceException {
    var request = createGetRequest("projects/summary")
        .header(HEADER_AUTHORIZATION, "Bearer " + getTokenValue())
        .build();
    try {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return JsonUtil.jsonToObject(response.body(), new TypeReference<List<ProjectDto>>() {
        });
      }
    } catch (IOException | InterruptedException e) {
      throw new LoadmanceException("Request error", e);
    }
    return null;
  }

  public List<TestBuilderDto> getTestBuilders(String projectId) throws LoadmanceException {
    var request = createGetRequest("test-builders?project-id=" + projectId)
        .header(HEADER_AUTHORIZATION, "Bearer " + getTokenValue())
        .build();
    try {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return JsonUtil.jsonToObject(response.body(), TestBuilderListResponse.class).getItems();
      }
    } catch (IOException | InterruptedException e) {
      throw new LoadmanceException("Request error", e);
    }
    return null;
  }

  public TestRunDto startTest(String testBuilderId) throws LoadmanceException {
    var request = createDefaultRequest("test-runs/run?" + "test-builder-id=" + testBuilderId)
        .POST(BodyPublishers.noBody())
        .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
        .header(HEADER_AUTHORIZATION, "Bearer " + getTokenValue())
        .build();

    try {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return JsonUtil.jsonToObject(response.body(), TestRunDto.class);
      }
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Error client request", e);
      throw new LoadmanceException("Request error", e);
    } finally {
      LOGGER.debug("Finish POST request to ={}", request.uri().toString());
    }
    return null;
  }


  public TestRunStatusDto getTestRunStatus(String testRunId) throws LoadmanceException {
    var request = createGetRequest("test-runs/" + testRunId)
        .header(HEADER_AUTHORIZATION, "Bearer " + getTokenValue())
        .build();

    try {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return JsonUtil.jsonToObject(response.body(), TestRunResponse.class).getTestRun();
      }
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Error client request", e);
      throw new LoadmanceException("Request error", e);
    } finally {
      LOGGER.debug("Finish POST request to ={}", request.uri().toString());
    }
    return null;
  }

  public boolean stopTest(String testRunId) throws LoadmanceException {
    var request = createDefaultRequest("test-runs/stop?" + "test-run-ids=" + testRunId)
        .POST(BodyPublishers.noBody())
        .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
        .header(HEADER_AUTHORIZATION, "Bearer " + getTokenValue())
        .build();

    try {
      var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return true;
      }
    } catch (IOException | InterruptedException e) {
      LOGGER.error("Error client request", e);
      throw new LoadmanceException("Request error", e);
    } finally {
      LOGGER.debug("Finish POST request to ={}", request.uri().toString());
    }
    return false;
  }

  private static HttpRequest.Builder createGetRequest(String path) {
    return createDefaultRequest(path)
        .GET();
  }

  private static <T> HttpRequest.Builder createPostRequest(String path, T body) throws LoadmanceException {
    return createDefaultRequest(path)
        .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.objectToJson(body)))
        .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE);
  }

  private static Builder createDefaultRequest(String path) {
    return HttpRequest.newBuilder()
        .uri(URI.create(AppUtil.INSTANCE.getBaseUrl() + path))
        .header(HEADER_ACCEPT, HEADER_ACCEPT_VALUE);
  }
}
