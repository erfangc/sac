package com.erfangc.sac.core.backend.inmemory;

import com.erfangc.sac.core.Group;
import com.erfangc.sac.core.Node;
import com.erfangc.sac.core.Policy;
import com.erfangc.sac.core.backend.Backend;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryBackend implements Backend {

    private ConcurrentHashMap<String, Policy> policies;
    private ConcurrentHashMap<String, PolicyAttachment> policyAttachments;
    private ConcurrentHashMap<String, Group> groups;
    private ConcurrentHashMap<String, GroupMembership> groupMemberships;

    public InMemoryBackend() {
        policies = new ConcurrentHashMap<>();
        policyAttachments = new ConcurrentHashMap<>();
        groups = new ConcurrentHashMap<>();
        groupMemberships = new ConcurrentHashMap<>();
    }

    @Override
    public List<String> resolvePolicyIdsForPrincipal(String principalId) {
        return null;
    }

    @Override
    public List<Policy> loadPolicies(List<String> policyIds) {
        return null;
    }

    @Override
    public void createGroup(Group group) {

    }

    @Override
    public Group getGroup(String id) {
        return null;
    }

    @Override
    public void update(Group group) {

    }

    @Override
    public void delete(String groupId) {

    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId) {

    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup) {

    }

    @Override
    public void unassignPrincipalToGroup(String groupId, String principalId) {

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

    }

    @Override
    public Policy getPolicy(String policyId) {
        return null;
    }

    @Override
    public void updatePolicy(Policy policy) {

    }

    @Override
    public void deletePolicy(String policyId) {

    }

    @Override
    public void assignPolicy(String policyId, String principalId) {

    }

    @Override
    public void unAssignPolicy(String policyId, String principalId) {

    }
}
