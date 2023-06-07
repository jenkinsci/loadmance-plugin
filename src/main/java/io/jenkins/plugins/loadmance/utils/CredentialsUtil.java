package io.jenkins.plugins.loadmance.utils;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.model.Item;
import hudson.security.ACL;
import io.jenkins.plugins.loadmance.LoadmanceCredentials;
import java.util.Optional;

public class CredentialsUtil {

  CredentialsUtil() {
    //no instance
  }

  public static Optional<LoadmanceCredentials> getSelected(Item item, String credentialsId) {
    return Optional.ofNullable(CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(
            LoadmanceCredentials.class, item, ACL.SYSTEM, new DomainRequirement()),
        CredentialsMatchers.withId(credentialsId)));
  }

}
