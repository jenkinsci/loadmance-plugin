package io.jenkins.plugins.loadmance;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.loadmance.exception.LoadmanceException;
import io.jenkins.plugins.loadmance.model.LoginRequestDto;
import io.jenkins.plugins.loadmance.model.ProjectDto;
import io.jenkins.plugins.loadmance.model.TestBuilderDto;
import io.jenkins.plugins.loadmance.service.LoadmanceService;
import io.jenkins.plugins.loadmance.utils.CredentialsUtil;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadmanceBuilder extends Builder implements SimpleBuildStep {

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
  public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
      @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {

    Result result;
    if (StringUtils.isBlank(getTestId())) {
      String message = "Test builder id is empty or not selected";
      listener.fatalError(message);
      logger.warn(message);
      run.setResult(Result.FAILURE);
      return;
    }

    if (StringUtils.isBlank(getProjectId())) {
      String message = "Project id is empty or not selected";
      listener.fatalError(message);
      logger.warn(message);
      run.setResult(Result.FAILURE);
      return;
    }

    if (StringUtils.isBlank(getCredentialsId())) {
      String message = "Credentials id is empty or not selected";
      listener.fatalError(message);
      logger.warn(message);
      run.setResult(Result.FAILURE);
      return;
    }

    var scope = CredentialsScope.GLOBAL;
    Item item = scope instanceof Item ? (Item) scope : null;
    var selectedCredentials = CredentialsProvider.lookupCredentials(LoadmanceCredentials.class, item, ACL.SYSTEM)
        .stream().filter(loadmanceCredentials -> loadmanceCredentials.getId().equals(getCredentialsId())).findFirst();

    if (selectedCredentials.isEmpty()) {
      String message = "Credentials not found";
      listener.fatalError(message);
      logger.warn(message);
      run.setResult(Result.FAILURE);
    }

    VirtualChannel c = launcher.getChannel();
    LoadmanceBuild loadmanceBuild = new LoadmanceBuild();
    loadmanceBuild.setListener(listener);
    loadmanceBuild.setTestId(getTestId());
    loadmanceBuild.setLoginRequestDto(new LoginRequestDto(selectedCredentials.get().getUsername(),
        selectedCredentials.get().getPassword()));

    try {
      if (c != null) {
        result = c.call(loadmanceBuild);
        run.setResult(result);
      }
    } catch (Exception e) {
      logger.error("Call loadmance builder error", e);
      throw new RuntimeException(e);
    }
    run.save();

  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  @Extension
  @Symbol("loadmance")
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Loadmance Test Builder";
    }


    @RequirePOST
    public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item,
        @QueryParameter("credentialsId") String credentialsId) {
      if (item == null) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      } else {
        item.checkAnyPermission(Item.CONFIGURE, Item.CREATE, Item.READ);
      }
      StandardListBoxModel items = new StandardListBoxModel();

      CredentialsProvider.lookupCredentials(LoadmanceCredentials.class, item, ACL.SYSTEM, new DomainRequirement())
          .forEach(loadmanceCredentials -> {
            boolean selected =
                !StringUtils.isBlank(credentialsId) && credentialsId.equals(loadmanceCredentials.getId());
            items.add(
                new ListBoxModel.Option(loadmanceCredentials.getDescription(), loadmanceCredentials.getId(), selected));
          });
      return items;
    }

    @RequirePOST
    public ListBoxModel doFillProjectIdItems(@AncestorInPath Item item,
        @QueryParameter("credentialsId") String credentialsId,
        @QueryParameter("projectId") String projectId) {
      if (item == null) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      } else {
        item.checkAnyPermission(Item.CONFIGURE, Item.CREATE, Item.READ);
      }

      StandardListBoxModel result = new StandardListBoxModel();

      if (StringUtils.isBlank(credentialsId) || credentialsId.equals("none")) {
        return result;
      }

      var selectedCredentials = CredentialsUtil.getSelected(item, credentialsId);

      if (selectedCredentials.isEmpty()) {
        return result;
      }

      LoadmanceService.getInstance().updateLoginRequestDto(new LoginRequestDto(selectedCredentials.get().getUsername(),
          selectedCredentials.get().getPassword()));
      try {
        var projects = LoadmanceService.getInstance().getProjects();
        for (ProjectDto project : projects) {
          boolean selected = !StringUtils.isBlank(projectId) && projectId.equals(project.getId());
          result.add(new ListBoxModel.Option(project.getTitle(), project.getId(), selected));
        }
      } catch (LoadmanceException e) {
        FormValidation.error(e.getMessage());
      }

      return result;
    }

    @RequirePOST
    public ListBoxModel doFillTestIdItems(@AncestorInPath Item item,
        @QueryParameter("credentialsId") String credentialsId,
        @QueryParameter("projectId") String projectId, @QueryParameter("testId") String testId) {
      if (item == null) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      } else {
        item.checkAnyPermission(Item.CONFIGURE, Item.CREATE, Item.READ);
      }

      StandardListBoxModel result = new StandardListBoxModel();

      if (StringUtils.isBlank(projectId) || StringUtils.isBlank(credentialsId) || credentialsId.equals("none")) {
        return result;
      }
      var selectedCredentials = CredentialsUtil.getSelected(item, credentialsId);

      if (selectedCredentials.isEmpty()) {
        return result;
      }

      LoadmanceService.getInstance().updateLoginRequestDto(new LoginRequestDto(selectedCredentials.get().getUsername(),
          selectedCredentials.get().getPassword()));

      try {
        var testBuilderDtoList = LoadmanceService.getInstance().getTestBuilders(projectId);

        for (TestBuilderDto testBuilderDto : testBuilderDtoList) {
          boolean selected = !StringUtils.isBlank(testId) && testId.equals(testBuilderDto.getId());
          result.add(new ListBoxModel.Option(testBuilderDto.getTitle(), testBuilderDto.getId(), selected));

        }
      } catch (LoadmanceException e) {
        FormValidation.error(e.getMessage());
      }

      return result;
    }


  }
}
