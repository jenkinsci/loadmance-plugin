package io.jenkins.plugins.loadmance;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.Secret;
import io.jenkins.plugins.loadmance.exception.LoadmanceException;
import io.jenkins.plugins.loadmance.service.LoadmanceService;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class LoadmanceCredentials extends BaseStandardCredentials implements StandardUsernamePasswordCredentials {

  private final String username;
  private final Secret password;

  @DataBoundConstructor
  public LoadmanceCredentials(CredentialsScope scope, String id, String description, String username, Secret password) {
    super(scope, id, description);
    this.username = username;
    this.password = password;
  }


  @NonNull
  @Override
  public Secret getPassword() {
    return password;
  }

  @NonNull
  @Override
  public String getUsername() {
    return username;
  }

  @Extension(ordinal = 1)
  public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Loadmance Credentials";
    }


    @RequirePOST
    public FormValidation doTestConnection(@AncestorInPath Item item, @QueryParameter("username") String username,
        @QueryParameter("password") String password) {
      if (item == null) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
          return FormValidation.ok(); // (2)
        }
      } else {
        item.checkAnyPermission(Item.CONFIGURE, Item.CREATE, Item.READ);
      }

      try {
        var response = LoadmanceService.getInstance().login(username, Secret.fromString(password));
        if (response == null || response.getToken() == null) {
          return FormValidation.ok("Connection error. Please check credentials");
        } else {
          return FormValidation.ok("Connection successful.");
        }
      } catch (LoadmanceException e) {
        return FormValidation.error("Connection error " + e.getMessage());
      }
    }

  }

}
