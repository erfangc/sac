package com.erfangc.sac.backend.redis;

import com.erfangc.sac.core.*;
import com.erfangc.sac.core.backend.Backend;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class RedisBackend implements Backend, Closeable {

    private final RedisClient client;
    private final RedisCommands<String, String> sync;
    private final ObjectMapper objectMapper;

    public RedisBackend(String serverEndpoint) {
        client = RedisClient.create("redis://" + serverEndpoint);
        sync = client.connect().sync();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public List<String> resolvePolicyIdsForPrincipal(String principalId) {
        return null;
    }

    @Override
    public List<Policy> loadPolicies(List<String> policyIds) {
        return policyIds
                .stream()
                .map(pid -> {
                    // TODO use Redis piplining to queue up the commands
                    final String json = sync.get(pid);
                    try {
                        return objectMapper.readValue(json, ImmutablePolicy.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(toList());
    }

    @Override
    public void createGroup(Group group) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(group);
            sync.set("group:" + group.id(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Group getGroup(String id) {
        final String json = sync.get("group:" + id);
        try {
            return objectMapper.readValue(json, ImmutableGroup.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGroup(Group group) {
        createGroup(group);
    }

    @Override
    public void deleteGroup(String groupId) {
        sync.del("group:" + groupId);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId) {
        assignPrincipalToGroup(groupId, principalId, false);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup) {
        sync.sadd("groupToPrincipalMap:" + groupId, principalId);
        if (principalIsGroup) {
            sync.sadd("groupToGroupMap:" + groupId, principalId);
        } else {
            sync.sadd("principalToGroupMap:" + principalId, groupId);
        }
    }

    @Override
    public void unassignPrincipalFromGroup(String groupId, String principalId) {
        sync.srem("groupToPrincipalMap:" + groupId, principalId);
        sync.srem("groupToGroupMap:" + groupId, principalId);
        sync.srem("principalToGroupMap:" + principalId, groupId);
    }

    @Override
    public List<String> getAllPrincipalsForGroup(String groupId) {
        return null;
    }

    @Override
    public List<String> getGroupMembership(String principalId) {
        final Set<String> set = sync.smembers("principalToGroupMap:" + principalId);
        return new ArrayList<>(set);
    }

    @Override
    public Node getGroupTree(String groupId) {
        return null;
    }

    @Override
    public void createPolicy(Policy policy) {
        try {
            final String json = objectMapper.writeValueAsString(policy);
            sync.set("policy:" + policy.id(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Policy getPolicy(String policyId) {
        final String json = sync.get("policy:" + policyId);
        try {
            return objectMapper.readValue(json, ImmutablePolicy.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePolicy(Policy policy) {
        createPolicy(policy);
    }

    @Override
    public void deletePolicy(String policyId) {
        sync.del(policyId);
    }

    @Override
    public void assignPolicy(String policyId, String principalId) {
        sync.sadd("policyToPrincipalMap:" + policyId, principalId);
        sync.sadd("principalToPolicyMap:" + principalId, policyId);
    }

    @Override
    public void unAssignPolicy(String policyId, String principalId) {
        sync.srem("policyToPrincipalMap:" + policyId, principalId);
        sync.srem("principalToPolicyMap:" + principalId, policyId);
    }

    @Override
    public void close() {
        sync.shutdown(true);
        client.shutdown();
    }
}
