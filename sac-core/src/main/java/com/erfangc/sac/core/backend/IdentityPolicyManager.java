package com.erfangc.sac.core.backend;


import com.erfangc.sac.interfaces.IdentityPolicy;

public interface IdentityPolicyManager {

    void createPolicy(IdentityPolicy identityPolicy);

    IdentityPolicy getPolicy(String policyId);

    void updatePolicy(IdentityPolicy identityPolicy);

    void deletePolicy(String policyId);

    void assignPolicy(String policyId, String principalId);

    void unAssignPolicy(String policyId, String principalId);

}
