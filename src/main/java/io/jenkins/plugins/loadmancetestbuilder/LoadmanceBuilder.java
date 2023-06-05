package io.jenkins.plugins.loadmancetestbuilder;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.loadmancetestbuilder.exception.LoadmanceException;
import io.jenkins.plugins.loadmancetestbuilder.model.AuthRequestDto;
import io.jenkins.plugins.loadmancetestbuilder.model.ProjectDto;
import io.jenkins.plugins.loadmancetestbuilder.model.TestBuilderDto;
import io.jenkins.plugins.loadmancetestbuilder.service.LoadmanceService;
import io.jenkins.plugins.loadmancetestbuilder.utils.CredentialsUtil;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadmanceBuilder extends Builder {

  private static final Logger logger = LoggerFactory.getLogger(LoadmanceBuilder.class.getSimpleName());

  private String credentialsId;
  private String projectId;
  private String testId;


  @DataBoundConstructor
  public LoadmanceBuilder(String credentialsId, String projectId, String testId) {
    this.credentialsId = credentialsId;
    this.projectId = projectId;
    this.testId = testId;
  }


  public String getCredentialsId() {
    return credentialsId;
  }

  public void setCredentialsId(String credentialsId) {
    this.credentialsId = credentialsId;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getTestId() {
    return testId;
  }

  public void setTestId(String testId) {
    this.testId = testId;
  }

  @Override
  public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener)
      throws InterruptedException, IOException {

    if (StringUtils.isBlank(getTestId())) {
      String message = "Test builder id is empty or not selected";
      listener.fatalError(message);
      logger.warn(message);
      build.setResult(Result.FAILURE);
      return false;
    }

    if (StringUtils.isBlank(getProjectId())) {
      String message = "Project id is empty or not selected";
      listener.fatalError(message);
      logger.warn(message);
      build.setResult(Result.FAILURE);
      return false;
    }

    if (StringUtils.isBlank(getCredentialsId())) {
      String message = "Credentials id is empty or not selected";
      listener.fatalError(message);
      logger.warn(message);
      build.setResult(Result.FAILURE);
      return false;
    }

    var scope = CredentialsScope.GLOBAL;
    Item item = scope instanceof Item ? (Item) scope : null;
    var selectedCredentials = CredentialsProvider.lookupCredentials(LoadmanceCredentials.class, item, ACL.SYSTEM)
        .stream().filter(loadmanceCredentials -> loadmanceCredentials.getId().equals(getCredentialsId())).findFirst();

    if (selectedCredentials.isEmpty()) {
      String message = "Credentials not found";
      listener.fatalError(message);
      logger.warn(message);
      build.setResult(Result.FAILURE);
      return false;
    }

    VirtualChannel c = launcher.getChannel();
    LoadmanceBuild loadmanceBuild = new LoadmanceBuild();
    loadmanceBuild.setListener(listener);
    loadmanceBuild.setTestId(getTestId());
    loadmanceBuild.setAuthRequestDto(new AuthRequestDto(selectedCredentials.get().getUsername(),
        selectedCredentials.get().getPassword().getPlainText()));

    Result result;
    try {
      result = c.call(loadmanceBuild);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    build.setResult(result);
    return true;
  }


  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Loadmance Test Builder";
    }

    public ListBoxModel doFillCredentialsIdItems(@QueryParameter("credentialsId") String credentialsId) {
      ListBoxModel items = new ListBoxModel();

      Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);

      CredentialsProvider.lookupCredentials(LoadmanceCredentials.class, item, ACL.SYSTEM)
          .forEach(loadmanceCredentials -> {
            boolean selected =
                !StringUtils.isBlank(credentialsId) && credentialsId.equals(loadmanceCredentials.getId());
            items.add(
                new ListBoxModel.Option(loadmanceCredentials.getDescription(), loadmanceCredentials.getId(), selected));
          });
      return items;
    }

    public ListBoxModel doFillProjectIdItems(@QueryParameter("credentialsId") String credentialsId,
        @QueryParameter("projectId") String projectId) {
      if (StringUtils.isBlank(credentialsId) || credentialsId.equals("none")) {
        return new ListBoxModel();
      }
      ListBoxModel listBoxModel = new ListBoxModel();

      var selectedCredentials = CredentialsUtil.getSelected(credentialsId);

      if (selectedCredentials.isEmpty()) {
        return new ListBoxModel();
      }

      LoadmanceService.INSTANCE.setAuthRequestDto(new AuthRequestDto(selectedCredentials.get().getUsername(),
          selectedCredentials.get().getPassword().getPlainText()));
      try {
        var projects = LoadmanceService.INSTANCE.getProjects();
        for (ProjectDto project : projects) {
          boolean selected = !StringUtils.isBlank(projectId) && projectId.equals(project.getId());
          listBoxModel.add(new ListBoxModel.Option(project.getTitle(), project.getId(), selected));
        }
      } catch (LoadmanceException e) {
        FormValidation.error(e.getMessage());
      }

      return listBoxModel;
    }

    public ListBoxModel doFillTestIdItems(@QueryParameter("credentialsId") String credentialsId,
        @QueryParameter("projectId") String projectId, @QueryParameter("testId") String testId) {
      if (StringUtils.isBlank(projectId) || StringUtils.isBlank(credentialsId) || credentialsId.equals("none")) {
        return new ListBoxModel();
      }
      ListBoxModel listBoxModel = new ListBoxModel();
      var selectedCredentials = CredentialsUtil.getSelected(credentialsId);

      if (selectedCredentials.isEmpty()) {
        return new ListBoxModel();
      }

      LoadmanceService.INSTANCE.setAuthRequestDto(new AuthRequestDto(selectedCredentials.get().getUsername(),
          selectedCredentials.get().getPassword().getPlainText()));

      try {
        var testBuilderDtoList = LoadmanceService.INSTANCE.getTestBuilders(projectId);

        for (TestBuilderDto testBuilderDto : testBuilderDtoList) {
          boolean selected = !StringUtils.isBlank(testId) && testId.equals(testBuilderDto.getId());
          listBoxModel.add(new ListBoxModel.Option(testBuilderDto.getTitle(), testBuilderDto.getId(), selected));

        }
      } catch (LoadmanceException e) {
        FormValidation.error(e.getMessage());
      }

      return listBoxModel;
    }


  }
}
