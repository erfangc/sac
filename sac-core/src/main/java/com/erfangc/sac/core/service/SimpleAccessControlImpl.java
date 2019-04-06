package com.erfangc.sac.core.service;

import com.erfangc.sac.core.backend.Backend;
import com.erfangc.sac.interfaces.*;

import java.util.List;

public class SimpleAccessControlImpl implements SimpleAccessControl {

    private Backend backend;
    private PolicyDecisionMaker policyDecisionMaker;

    public SimpleAccessControlImpl(Backend backend) {
        this.backend = backend;
        policyDecisionMaker = new PolicyDecisionMaker();
    }

    @Override
    public void createGroup(Group group) {
        backend.createGroup(group);
    }

    @Override
    public Group getGroup(String id) {
        return backend.getGroup(id);
    }

    @Override
    public void updateGroup(Group group) {
        backend.updateGroup(group);
    }

    @Override
    public void deleteGroup(String groupId) {
        backend.deleteGroup(groupId);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId) {
        backend.assignPrincipalToGroup(groupId, principalId);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup) {
        backend.assignPrincipalToGroup(groupId, principalId, principalIsGroup);
    }

    @Override
    public void unassignPrincipalFromGroup(String groupId, String principalId) {
        backend.unassignPrincipalFromGroup(groupId, principalId);
    }

    @Override
    public List<String> getAllPrincipalsForGroup(String groupId) {
        return backend.getAllPrincipalsForGroup(groupId);
    }

    @Override
    public List<String> getGroupMembership(String principalId) {
        return backend.getGroupMembership(principalId);
    }

    @Override
    public List<String> getGroupMembershipTransitively(String principalId) {
        return backend.getGroupMembershipTransitively(principalId);
    }

    @Override
    public Node getGroupTree(String groupId) {
        return backend.getGroupTree(groupId);
    }

    @Override
    public void createPolicy(IdentityPolicy identityPolicy) {
        backend.createPolicy(identityPolicy);
    }

    @Override
    public IdentityPolicy getPolicy(String policyId) {
        return backend.getPolicy(policyId);
    }

    @Override
    public void updatePolicy(IdentityPolicy identityPolicy) {
        backend.updatePolicy(identityPolicy);
    }

    @Override
    public void deletePolicy(String policyId) {
        backend.deletePolicy(policyId);
    }

    @Override
    public void assignPolicy(String policyId, String principalId) {
        backend.assignPolicy(policyId, principalId);
    }

    @Override
    public void unAssignPolicy(String policyId, String principalId) {
        backend.unAssignPolicy(policyId, principalId);
    }

    @Override
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        final List<IdentityPolicy> policies = backend.fetchIdentityPoliciesTransitivelyForPrincipal(request.principal());
        return policyDecisionMaker.makeAccessDecision(request, policies);
    }
}
