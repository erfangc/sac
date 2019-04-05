package com.erfangc.sac.interfaces;

import com.erfangc.sac.interfaces.*;

import java.util.List;

/**
 * The main interface for performing access control functions
 */
public interface SimpleAccessControl {

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

    AuthorizationResponse authorize(AuthorizationRequest request);

}
