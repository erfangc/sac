package com.erfangc.sac.core.service;

import com.erfangc.sac.core.backend.inmemory.InMemoryBackend;
import com.erfangc.sac.interfaces.*;

import java.util.List;
import java.util.Set;

public class InMemorySimpleAccessControl implements SimpleAccessControl {

    private static InMemorySimpleAccessControl instance = new InMemorySimpleAccessControl();
    private final SimpleAccessControlImpl delegate;

    private InMemorySimpleAccessControl() {
        delegate = new SimpleAccessControlImpl(new InMemoryBackend());
    }

    public static synchronized InMemorySimpleAccessControl getInstance() {
        return instance;
    }

    @Override
    public void createGroup(Group group) {
        delegate.createGroup(group);
    }

    @Override
    public Group getGroup(String id) {
        return delegate.getGroup(id);
    }

    @Override
    public void updateGroup(Group group) {
        delegate.updateGroup(group);
    }

    @Override
    public void deleteGroup(String groupId) {
        delegate.deleteGroup(groupId);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId) {
        delegate.assignPrincipalToGroup(groupId, principalId);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup) {
        delegate.assignPrincipalToGroup(groupId, principalId, principalIsGroup);
    }

    @Override
    public void unassignPrincipalFromGroup(String groupId, String principalId) {
        delegate.unassignPrincipalFromGroup(groupId, principalId);
    }

    @Override
    public List<String> getAllPrincipalsForGroup(String groupId) {
        return delegate.getAllPrincipalsForGroup(groupId);
    }

    @Override
    public List<String> getGroupMembership(String principalId) {
        return delegate.getGroupMembership(principalId);
    }

    @Override
    public List<String> getGroupMembershipTransitively(String principalId) {
        return null;
    }

    @Override
    public Node getGroupTree(String groupId) {
        return delegate.getGroupTree(groupId);
    }

    @Override
    public void createPolicy(IdentityPolicy identityPolicy) {
        delegate.createPolicy(identityPolicy);
    }

    @Override
    public IdentityPolicy getPolicy(String policyId) {
        return delegate.getPolicy(policyId);
    }

    @Override
    public void updatePolicy(IdentityPolicy identityPolicy) {
        delegate.updatePolicy(identityPolicy);
    }

    @Override
    public void deletePolicy(String policyId) {
        delegate.deletePolicy(policyId);
    }

    @Override
    public void assignPolicy(String policyId, String principalId) {
        delegate.assignPolicy(policyId, principalId);
    }

    @Override
    public void unAssignPolicy(String policyId, String principalId) {
        delegate.unAssignPolicy(policyId, principalId);
    }

    @Override
    public AuthorizationResponse authorize(AuthorizationRequest request) {
        return delegate.authorize(request);
    }

    @Override
    public void grantActions(String resource, String principal, Set<String> actions) {
        delegate.grantActions(resource, principal, actions);
    }

    @Override
    public void revokeActions(String resource, String principal, Set<String> actions) {
        delegate.revokeActions(resource, principal, actions);
    }

    @Override
    public ResourcePolicy getResourcePolicy(String resource) {
        return null;
    }
}
