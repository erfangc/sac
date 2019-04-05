package com.erfangc.sac.redis;

import com.erfangc.sac.backend.redis.RedisBackend;
import com.erfangc.sac.core.backend.Backend;
import com.erfangc.sac.interfaces.SimpleAccessControl;
import com.erfangc.sac.core.service.SimpleAccessControlImpl;
import com.erfangc.sac.interfaces.*;

import java.io.Closeable;
import java.util.List;

public class RedisSimpleAccessControl implements SimpleAccessControl, Closeable {

    private static final String endpoint = System.getProperty("sac.redis.endpoint", "localhost");

    private static RedisSimpleAccessControl ourInstance = new RedisSimpleAccessControl();
    private final Backend backend;
    private final SimpleAccessControl delegate;

    private RedisSimpleAccessControl() {
        backend = new RedisBackend(endpoint);
        delegate = new SimpleAccessControlImpl(backend);
    }

    public static RedisSimpleAccessControl getInstance() {
        return ourInstance;
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
    public Node getGroupTree(String groupId) {
        return delegate.getGroupTree(groupId);
    }

    @Override
    public void createPolicy(Policy policy) {
        delegate.createPolicy(policy);
    }

    @Override
    public Policy getPolicy(String policyId) {
        return delegate.getPolicy(policyId);
    }

    @Override
    public void updatePolicy(Policy policy) {
        delegate.updatePolicy(policy);
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
    public void close() {
        ((RedisBackend) backend).close();
    }
}
