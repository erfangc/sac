package com.erfangc.sac.core.service;

import com.erfangc.sac.core.*;

import java.util.List;

/**
 * The main interface for performing access control functions
 */
public interface SimpleAccessControl {

    void createGroup(Group group);

    Group getGroup(String id);

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

    AuthorizationResponse authorize(AuthorizationRequest request);

}
