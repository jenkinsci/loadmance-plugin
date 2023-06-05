package io.jenkins.plugins.loadmance.utils;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Item;
import hudson.security.ACL;
import io.jenkins.plugins.loadmance.LoadmanceCredentials;
import java.util.Optional;
import org.kohsuke.stapler.Stapler;

public class CredentialsUtil {


  public static Optional<LoadmanceCredentials> getSelected(String credentialsId) {
    Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
    Optional<LoadmanceCredentials> selectedCredentials = CredentialsProvider.lookupCredentials(
            LoadmanceCredentials.class, item, ACL.SYSTEM).stream()
        .filter(loadmanceCredentials -> loadmanceCredentials.getId().equals(credentialsId)).findFirst();
    return selectedCredentials;
  }

}
