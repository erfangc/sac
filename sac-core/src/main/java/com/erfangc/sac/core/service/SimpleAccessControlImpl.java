package com.erfangc.sac.core.service;

import com.erfangc.sac.core.*;
import com.erfangc.sac.core.backend.Backend;

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
        return null;
    }

    @Override
    public void update(Group group) {
        backend.update(group);
    }

    @Override
    public void delete(String groupId) {
        backend.delete(groupId);
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
    public void unassignPrincipalToGroup(String groupId, String principalId) {
        backend.unassignPrincipalToGroup(groupId, principalId);
    }

    @Override
    public List<String> getAllPrincipalsForGroup(String groupId) {
        return null;
    }

    @Override
    public List<String> getGroupMembership(String principalId) {
        return null;
    }

    @Override
    public List<Node> getGroupTree(String groupId) {
        return null;
    }

    @Override
    public void createPolicy(Policy policy) {
        backend.createPolicy(policy);
    }

    @Override
    public Policy getPolicy(Policy policy) {
        return null;
    }

    @Override
    public void updatePolicy(Policy policy) {
        backend.updatePolicy(policy);
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
        final List<String> policyIds = backend.resolvePolicyIdsForPrincipal(request.principal());
        final List<Policy> policies = backend.loadPolicies(policyIds);
        return policyDecisionMaker.makeAccessDecision(request, policies);
    }
}
