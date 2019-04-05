package com.erfangc.sac.core.backend;


import com.erfangc.sac.interfaces.Group;
import com.erfangc.sac.interfaces.Node;
import com.erfangc.sac.interfaces.Policy;

import java.util.List;

public interface Backend {

    List<String> resolvePolicyIdsForPrincipal(String principalId);

    List<Policy> loadPolicies(List<String> policyIds);

    void createGroup(Group group);

    Group getGroup(String id);

    void updateGroup(Group group);

    void deleteGroup(String groupId);

    void assignPrincipalToGroup(String groupId, String principalId);

    void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup);

    void unassignPrincipalFromGroup(String groupId, String principalId);

    List<String> getAllPrincipalsForGroup(String groupId);

    List<String> getGroupMembership(String principalId);

    Node getGroupTree(String groupId);

    void createPolicy(Policy policy);

    Policy getPolicy(String policyId);

    void updatePolicy(Policy policy);

    void deletePolicy(String policyId);

    void assignPolicy(String policyId, String principalId);

    void unAssignPolicy(String policyId, String principalId);

}
