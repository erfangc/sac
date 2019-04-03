package com.erfangc.sac.core.backend;

import com.erfangc.sac.core.Group;
import com.erfangc.sac.core.Node;
import com.erfangc.sac.core.Policy;

import java.util.List;

public interface Backend {

    List<String> resolvePolicyIdsForPrincipal(String principalId);

    List<Policy> loadPolicies(List<String> policyIds);

    void createGroup(Group group);

    Group getGroup(Group id);

    void update(Group group);

    void delete(String groupId);

    void assignPrincipalToGroup(String groupId, String principalId);

    void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup);

    void unassignPrincipalToGroup(String groupId, String principalId);

    List<String> getAllPrincipalsForGroup(String groupId);

    List<String> getGroupMembership(String principalId);

    List<Node> getGroupTree(String groupId);

    void createPolicy(Policy policy);

    Policy getPolicy(Policy policy);

    void updatePolicy(Policy policy);

    void deletePolicy(String policyId);

    void assignPolicy(String policyId, String principalId);

    void unAssignPolicy(String policyId, String principalId);

}
