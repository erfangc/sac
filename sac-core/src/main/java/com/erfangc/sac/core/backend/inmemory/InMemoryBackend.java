package com.erfangc.sac.core.backend.inmemory;

import com.erfangc.sac.core.backend.Backend;
import com.erfangc.sac.interfaces.*;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;

public class InMemoryBackend implements Backend {

    private Map<String, Group> groups;
    private Map<String, Policy> policies;

    private Map<String, Map<String, String>> groupToPrincipalMap;
    private Map<String, Map<String, Group>> groupToGroupMap;
    private Map<String, Map<String, Group>> principalToGroupMap;
    private Map<String, Map<String, String>> policyToPrincipalMap;
    private Map<String, Map<String, Policy>> principalToPolicyMap;

    public InMemoryBackend() {
        policies = new HashMap<>();
        groups = new HashMap<>();
        groupToGroupMap = new HashMap<>();
        groupToPrincipalMap = new HashMap<>();
        principalToGroupMap = new HashMap<>();
        policyToPrincipalMap = new HashMap<>();
        principalToPolicyMap = new HashMap<>();
    }

    @Override
    public List<String> resolvePolicyIdsForPrincipal(String principalId) {
        final Map<String, Group> m1 = principalToGroupMap.get(principalId);
        // BFS to to construct a list of group membership and transitive group memberships for this principal
        // gids = groupIds, represents the group Ids the BFS has seen so far, used to both keep track of the results of the
        // traversal but also mark visited 'nodes' and prevent them from being processed again
        Set<String> gids = new HashSet<>();
        if (m1 != null) {
            Queue<String> queue = m1.values().stream().map(Group::id).collect(toCollection(ArrayDeque::new));
            while (!queue.isEmpty()) {
                final String gid = queue.poll();
                gids.add(gid);
                if (principalToGroupMap.containsKey(gid)) {
                    for (String cGid : principalToGroupMap.get(gid).keySet()) {
                        if (!gids.contains(cGid)) {
                            queue.add(cGid);
                        }
                    }
                }
            }
        }
        Set<String> ret = principalToPolicyMap.containsKey(principalId) ? principalToPolicyMap
                .get(principalId)
                .values()
                .stream()
                .map(Policy::id)
                .collect(toSet()) : new HashSet<>();
        gids.forEach(gid -> ret.addAll(principalToPolicyMap.getOrDefault(gid, emptyMap()).values().stream().map(Policy::id).collect(toList())));
        return new ArrayList<>(ret);
    }

    @Override
    public List<Policy> loadPolicies(List<String> policyIds) {
        return policyIds
                .stream()
                .map(id -> policies.get(id))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public void createGroup(Group group) {
        groups.put(group.id(), group);
    }

    @Override
    public Group getGroup(String groupId) {
        if (!groups.containsKey(groupId)) {
            return null;
        }
        final Map<String, Group> g2g = groupToGroupMap.getOrDefault(groupId, new HashMap<>());
        final Map<String, String> g2p = groupToPrincipalMap.getOrDefault(groupId, new HashMap<>());
        List<GroupAssignment> assignments = new ArrayList<>();
        g2g.values().forEach(a -> assignments.add(ImmutableGroupAssignment.builder().principal(a.id()).principalIsGroup(true).groupId(groupId).build()));
        g2p.values().forEach(p -> assignments.add(ImmutableGroupAssignment.builder().principal(p).groupId(groupId).build()));
        return ImmutableGroup.copyOf(groups.get(groupId))
                .withAssignments(assignments);
    }

    @Override
    public void updateGroup(Group group) {
        groups.put(group.id(), group);
    }

    @Override
    public void deleteGroup(String groupId) {
        groups.remove(groupId);
    }

    @Override
    public synchronized void assignPrincipalToGroup(String groupId, String principalId) {
        this.assignPrincipalToGroup(groupId, principalId, false);
    }

    @Override
    public synchronized void assignPrincipalToGroup(String groupId,
                                                    String principalId,
                                                    boolean principalIsGroup) {
        if (principalIsGroup) {
            final Map<String, Group> m1 = groupToGroupMap.getOrDefault(groupId, new HashMap<>());
            m1.put(principalId, this.getGroup(principalId));
            groupToGroupMap.put(groupId, m1);
        } else {
            final Map<String, String> m2 = groupToPrincipalMap.getOrDefault(groupId, new HashMap<>());
            m2.put(principalId, principalId);
            groupToPrincipalMap.put(groupId, m2);
        }
        final Map<String, Group> m3 = principalToGroupMap.getOrDefault(principalId, new HashMap<>());
        m3.put(groupId, this.getGroup(groupId));
        principalToGroupMap.put(principalId, m3);
    }

    @Override
    public synchronized void unassignPrincipalFromGroup(String groupId, String principalId) {
        final Map<String, Group> m1 = groupToGroupMap.getOrDefault(groupId, new HashMap<>());
        m1.remove(principalId);
        groupToGroupMap.put(groupId, m1);
        final Map<String, String> m2 = groupToPrincipalMap.getOrDefault(groupId, new HashMap<>());
        m2.remove(principalId);
        groupToPrincipalMap.put(groupId, m2);
        final Map<String, Group> m3 = principalToGroupMap.getOrDefault(principalId, new HashMap<>());
        m3.remove(groupId);
        principalToGroupMap.put(principalId, m3);
    }

    @Override
    public List<String> getAllPrincipalsForGroup(String groupId) {
        return new ArrayList<>(groupToPrincipalMap.get(groupId).keySet());
    }

    @Override
    public List<String> getGroupMembership(String principalId) {
        return new ArrayList<>(principalToGroupMap.get(principalId).keySet());
    }

    @Override
    public Node getGroupTree(String groupId) {
        Node root = new Node().setChildren(new ArrayList<>()).setName(groupId);
        Stack<Node> stack = new Stack<>();
        stack.add(root);
        Set<String> seen = new HashSet<>();
        while (!stack.isEmpty()) {
            final Node node = stack.pop();
            seen.add(node.getName());
            if (groupToGroupMap.containsKey(node.getName())) {
                final Set<String> childGids = groupToGroupMap.get(node.getName()).keySet();
                for (String childGid : childGids) {
                    if (!seen.contains(childGid)) {
                        final Node childNode = new Node().setName(childGid).setChildren(new ArrayList<>());
                        node.getChildren().add(childNode);
                        stack.add(childNode);
                    }
                }
            }
        }
        return root;
    }

    @Override
    public void createPolicy(Policy policy) {
        policies.put(policy.id(), policy);
    }

    @Override
    public Policy getPolicy(String policyId) {
        return policies.get(policyId);
    }

    @Override
    public void updatePolicy(Policy policy) {
        policies.put(policy.id(), policy);
    }

    @Override
    public void deletePolicy(String policyId) {
        policies.remove(policyId);
    }

    @Override
    public synchronized void assignPolicy(String policyId, String principalId) {
        final Map<String, String> m1 = policyToPrincipalMap.getOrDefault(policyId, new HashMap<>());
        final Map<String, Policy> m2 = principalToPolicyMap.getOrDefault(principalId, new HashMap<>());
        m1.put(policyId, principalId);
        m2.put(principalId, getPolicy(policyId));
        policyToPrincipalMap.put(policyId, m1);
        principalToPolicyMap.put(principalId, m2);
    }

    @Override
    public synchronized void unAssignPolicy(String policyId, String principalId) {
        final Map<String, String> m1 = policyToPrincipalMap.getOrDefault(policyId, new HashMap<>());
        final Map<String, Policy> m2 = principalToPolicyMap.getOrDefault(principalId, new HashMap<>());
        m1.remove(policyId);
        m2.remove(principalId);
    }
}
