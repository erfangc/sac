package com.erfangc.sac.core.backend;

import com.erfangc.sac.interfaces.IdentityPolicy;

import java.util.List;

/**
 * {@link Backend} is the aggregating interface that incorporates methods and features from all the
 * underlying services that compose a fully functioning backend, including managing resource based policies, identity based policies
 * as well as groups
 */
public interface Backend extends GroupManager, ResourcePolicyManager, IdentityPolicyManager {
    List<IdentityPolicy> fetchIdentityPoliciesTransitivelyForPrincipal(String principalId);
}
