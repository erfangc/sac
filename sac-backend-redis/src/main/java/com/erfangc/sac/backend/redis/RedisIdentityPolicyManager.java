package com.erfangc.sac.backend.redis;

import com.erfangc.sac.core.backend.Backend;
import com.erfangc.sac.interfaces.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class RedisIdentityPolicyManager implements Backend, Closeable {

    private static final String PRINCIPAL_TO_POLICY_MAP = "PRINCIPAL_TO_POLICY_MAP:";
    private static final String POLICY_TO_PRINCIPAL_MAP = "POLICY_TO_PRINCIPAL_MAP:";
    private static final String PRINCIPAL_TO_GROUP_MAP = "PRINCIPAL_TO_GROUP_MAP:";
    private static final String GROUP_TO_PRINCIPAL_MAP = "GROUP_TO_PRINCIPAL_MAP:";
    private static final String GROUP_TO_GROUP_MAP = "GROUP_TO_GROUP_MAP:";
    private static final String POLICY = "POLICY:";
    private static final String GROUP = "GROUP:";
    private final RedisClient client;
    private final RedisCommands<String, String> sync;
    private final ObjectMapper objectMapper;

    public RedisIdentityPolicyManager(String serverEndpoint) {
        client = RedisClient.create("redis://" + serverEndpoint);
        sync = client.connect().sync();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    private List<String> resolvePolicyIdsForPrincipal(String principalId) {
        // get the list of policies the principal is directly entitled to
        final Set<String> self = sync.smembers(PRINCIPAL_TO_POLICY_MAP + principalId);
        List<String> gids = getGroupMembershipTransitively(principalId);
        return Stream
                .concat(self.stream(), gids.stream().flatMap(gid -> sync.smembers(PRINCIPAL_TO_POLICY_MAP + gid).stream()))
                .distinct()
                .collect(toList());
    }

    private List<IdentityPolicy> loadPolicies(List<String> policyIds) {
        return policyIds
                .stream()
                .map(pid -> {
                    // TODO use Redis piplining to queue up the commands
                    final String json = sync.get(POLICY + pid);
                    if (json == null) {
                        return null;
                    }
                    try {
                        return objectMapper.readValue(json, ImmutableIdentityPolicy.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public void createGroup(Group group) {
        final String json;
        // we do not persist group memberships de-normalized
        // since the data structure is normalized for look up efficiency
        try {
            json = objectMapper.writeValueAsString(
                    ImmutableGroup.copyOf(group).withAssignments(Collections.emptyList())
            );
            sync.set(GROUP + group.id(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Group getGroup(String id) {
        final String json = sync.get(GROUP + id);
        try {
            final Set<String> pMembers = sync.smembers(GROUP_TO_PRINCIPAL_MAP + id);
            final Set<String> gMembers = sync.smembers(GROUP_TO_GROUP_MAP + id);
            if (json == null) {
                return null;
            }
            final ImmutableGroup group = objectMapper.readValue(json, ImmutableGroup.class);
            return group.withAssignments(
                    Stream.concat(
                            pMembers
                                    .stream()
                                    .map(
                                            p -> ImmutableGroupAssignment.builder().groupId(id).principal(p).build()),
                            gMembers
                                    .stream()
                                    .map(
                                            p -> ImmutableGroupAssignment.builder().groupId(id).principalIsGroup(true).principal(p).build()
                                    )
                    ).collect(toList())
            );
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
        sync.del(GROUP + groupId);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId) {
        assignPrincipalToGroup(groupId, principalId, false);
    }

    @Override
    public void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup) {
        sync.sadd(GROUP_TO_PRINCIPAL_MAP + groupId, principalId);
        if (principalIsGroup) {
            sync.sadd(GROUP_TO_GROUP_MAP + principalId, groupId);
        } else {
            sync.sadd(PRINCIPAL_TO_GROUP_MAP + principalId, groupId);
        }
    }

    @Override
    public void unassignPrincipalFromGroup(String groupId, String principalId) {
        sync.srem(GROUP_TO_PRINCIPAL_MAP + groupId, principalId);
        sync.srem(GROUP_TO_GROUP_MAP + groupId, principalId);
        sync.srem(PRINCIPAL_TO_GROUP_MAP + principalId, groupId);
    }

    @Override
    public List<String> getAllPrincipalsForGroup(String groupId) {
        return new ArrayList<>(sync.smembers(GROUP_TO_PRINCIPAL_MAP + groupId));
    }

    @Override
    public List<String> getGroupMembership(String principalId) {
        final Set<String> set = sync.smembers(PRINCIPAL_TO_GROUP_MAP + principalId);
        return new ArrayList<>(set);
    }

    @Override
    public Node getGroupTree(String groupId) {
        final Set<String> seen = new HashSet<>();
        Stack<Node> stack = new Stack<>();
        Node root = new Node().setName(groupId).setChildren(new ArrayList<>());
        stack.add(root);
        while (!stack.isEmpty()) {
            final Node node = stack.pop();
            seen.add(node.getName());
            for (String childGid : sync.smembers(GROUP_TO_PRINCIPAL_MAP + node.getName())) {
                if (!seen.contains(childGid)) {
                    Node childNode = new Node().setName(childGid).setChildren(new ArrayList<>());
                    node.getChildren().add(childNode);
                    stack.add(childNode);
                }
            }
        }
        return root;
    }

    @Override
    public void createPolicy(IdentityPolicy identityPolicy) {
        try {
            final String json = objectMapper.writeValueAsString(identityPolicy);
            sync.set(POLICY + identityPolicy.id(), json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public IdentityPolicy getPolicy(String policyId) {
        final String json = sync.get(POLICY + policyId);
        try {
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, ImmutableIdentityPolicy.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePolicy(IdentityPolicy identityPolicy) {
        createPolicy(identityPolicy);
    }

    @Override
    public void deletePolicy(String policyId) {
        sync.del(POLICY + policyId);
    }

    @Override
    public void assignPolicy(String policyId, String principalId) {
        sync.sadd(POLICY_TO_PRINCIPAL_MAP + policyId, principalId);
        sync.sadd(PRINCIPAL_TO_POLICY_MAP + principalId, policyId);
    }

    @Override
    public void unAssignPolicy(String policyId, String principalId) {
        sync.srem(POLICY_TO_PRINCIPAL_MAP + policyId, principalId);
        sync.srem(PRINCIPAL_TO_POLICY_MAP + principalId, policyId);
    }

    @Override
    public List<String> getGroupMembershipTransitively(String principalId) {
        final Set<String> gids = new HashSet<>();
        final Set<String> immediateMembership = sync.smembers(PRINCIPAL_TO_GROUP_MAP + principalId);
        final Queue<String> queue = new ArrayDeque<>(immediateMembership);
        // TODO this operation is O(M) where M = # of edges connecting the groups, maybe there is a way to optimize
        while (!queue.isEmpty()) {
            final String gid = queue.poll();
            gids.add(gid);
            for (String childGid : sync.smembers(GROUP_TO_GROUP_MAP + gid)) {
                if (!gids.contains(childGid)) {
                    queue.offer(childGid);
                }
            }
        }
        return new ArrayList<>(gids);
    }

    @Override
    public void close() {
        sync.shutdown(true);
        client.shutdown();
    }

    @Override
    public List<IdentityPolicy> fetchIdentityPoliciesTransitivelyForPrincipal(String principalId) {
        final List<String> policyIds = resolvePolicyIdsForPrincipal(principalId);
        return loadPolicies(policyIds);
    }
}
