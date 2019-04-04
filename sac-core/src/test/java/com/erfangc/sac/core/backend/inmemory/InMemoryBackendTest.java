package com.erfangc.sac.core.backend.inmemory;

import com.erfangc.sac.core.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;

public class InMemoryBackendTest {

    private InMemoryBackend backend;

    @Before
    public void setUp() {
        backend = new InMemoryBackend();
        final Group networkAdmins = networkAdmins();
        backend.createGroup(networkAdmins);
        final Group humanResources = humanResources();
        backend.createGroup(humanResources);
        final Policy employeeReadOnlyPolicy = employeeReadOnlyPolicy();
        backend.createPolicy(employeeReadOnlyPolicy);
        final Policy serverLoginPolicy = serverLoginPolicy();
        backend.createPolicy(serverLoginPolicy);
        final Policy managePayPolicy = managePayPolicy();
        backend.createPolicy(managePayPolicy);
        backend.assignPolicy(employeeReadOnlyPolicy.id(), networkAdmins.id());
        backend.assignPolicy(serverLoginPolicy.id(), networkAdmins.id());
        backend.assignPolicy(employeeReadOnlyPolicy.id(), humanResources.id());
        backend.assignPolicy(managePayPolicy.id(), humanResources.id());
    }

    private Policy managePayPolicy() {
        return ImmutablePolicy
                .builder()
                .id("manage pay")
                .actions(asList("increase", "decrease"))
                .resource("/org/employees/*/pay")
                .build();
    }

    private Policy serverLoginPolicy() {
        return ImmutablePolicy
                .builder()
                .id("server login")
                .actions(singletonList("login"))
                .resource("/org/servers/*")
                .build();
    }

    private Policy employeeReadOnlyPolicy() {
        return ImmutablePolicy
                .builder()
                .id("employee read only")
                .actions(singletonList("read"))
                .resource("/org/employees/*")
                .build();
    }

    private Group humanResources() {
        return ImmutableGroup
                .builder()
                .name("Human Resources")
                .id("hr")
                .build();
    }

    private Group networkAdmins() {
        return ImmutableGroup
                .builder()
                .name("Network Administrators")
                .id("network admins")
                .build();
    }

    @Test
    public void resolvePolicyIdsForPrincipal() {
        final ImmutableGroup allEmployees = allEmployees();
        backend.createGroup(allEmployees);
        backend.assignPolicy(employeeReadOnlyPolicy().id(), allEmployees.id());
        backend.assignPrincipalToGroup(allEmployees.id(), networkAdmins().id(), true);
        backend.assignPrincipalToGroup(networkAdmins().id(), "john");
        final List<String> policyIds = backend.resolvePolicyIdsForPrincipal("john");
        assertEquals(Stream.of("server login", "employee read only").collect(toSet()), new HashSet<>(policyIds));
    }

    @Test
    public void loadPolicies() {
        final List<Policy> policies = backend.loadPolicies(asList("server login", "employee read only"));
        assertEquals(2, policies.size());
    }

    @Test
    public void createGroup() {
        final ImmutableGroup group = ImmutableGroup.builder().id("new group").name("New Group").build();
        backend.createGroup(group);
        final Group result = backend.getGroup(group.id());
        assertEquals(group.id(), result.id());
    }

    @Test
    public void getGroup() {
        final Group group = backend.getGroup(networkAdmins().id());
        assertEquals("Network Administrators", group.name());
    }

    @Test
    public void update() {
        final String groupId = networkAdmins().id();
        final Group group = backend.getGroup(groupId);
        backend.updateGroup(
                ImmutableGroup
                        .copyOf(group)
                        .withName("Foo")
        );
        final Group result = backend.getGroup(groupId);
        assertEquals("Foo", result.name());
    }

    @Test
    public void deleteGroup() {
        backend.deleteGroup(networkAdmins().id());
        final Group group = backend.getGroup(networkAdmins().id());
        assertNull(group);
    }

    @Test
    public void assignPrincipalToGroup() {
        final Group group = networkAdmins();
        backend.assignPrincipalToGroup(group.id(), "john");
        final Group result = backend.getGroup(group.id());
        final Optional<List<GroupAssignment>> assignments = result.assignments();
        assertTrue(assignments.isPresent());
        final Optional<GroupAssignment> john = assignments.get().stream().filter(r -> r.principal().equals("john")).findFirst();
        assertTrue(john.isPresent());
    }

    @Test
    public void unassignPrincipalToGroup() {
        final Group group = networkAdmins();
        backend.assignPrincipalToGroup(group.id(), "john");
        final Group result = backend.getGroup(group.id());
        final Optional<List<GroupAssignment>> assignments = result.assignments();
        assertTrue(assignments.isPresent());
        final Optional<GroupAssignment> john = assignments.get().stream().filter(r -> r.principal().equals("john")).findFirst();
        assertTrue(john.isPresent());

        backend.unassignPrincipalFromGroup(group.id(), "john");
        final Group result2 = backend.getGroup(group.id());
        final Optional<List<GroupAssignment>> assignments2 = result2.assignments();
        assertTrue(assignments2.isPresent());
        final Optional<GroupAssignment> john2 = assignments2.get().stream().filter(r -> r.principal().equals("john")).findFirst();
        assertFalse(john2.isPresent());
    }

    @Test
    public void getAllPrincipalsForGroup() {
        final Group group = networkAdmins();
        backend.assignPrincipalToGroup(group.id(), "john");
        backend.assignPrincipalToGroup(group.id(), "jsmith");
        final List<String> principals = backend.getAllPrincipalsForGroup(group.id());
        assertTrue(principals.contains("john"));
        assertTrue(principals.contains("jsmith"));
    }

    @Test
    public void getGroupMembership() {
        final Group networkAdmins = networkAdmins();
        final Group hr = humanResources();
        backend.assignPrincipalToGroup(networkAdmins.id(), "john");
        backend.assignPrincipalToGroup(hr.id(), "john");
        final List<String> groups = backend.getGroupMembership("john");
        assertEquals(2, groups.size());
        assertTrue(groups.contains(networkAdmins.id()));
        assertTrue(groups.contains(hr.id()));
    }

    @Test
    public void getGroupTree() {
        final ImmutableGroup allEmployees = allEmployees();
        backend.createGroup(allEmployees);
        backend.assignPrincipalToGroup(allEmployees.id(), networkAdmins().id(), true);
        backend.assignPrincipalToGroup(allEmployees.id(), humanResources().id(), true);
        Node root = backend.getGroupTree(allEmployees.id());
        assertNotNull(root);
        assertEquals(2, root.getChildren().size());
    }

    private ImmutableGroup allEmployees() {
        return ImmutableGroup.
                builder()
                .id("all employees")
                .name("All Employees")
                .build();
    }

    @Test
    public void createPolicy() {
        Policy newPolicy = ImmutablePolicy
                .builder()
                .id("new policy")
                .resource("/foo/bar")
                .actions(singletonList("*"))
                .build();
        backend.createPolicy(newPolicy);
        final Policy policy = backend.getPolicy(newPolicy.id());
        assertEquals("new policy", policy.id());
    }

    @Test
    public void getPolicy() {
        final Policy policy = backend.getPolicy(managePayPolicy().id());
        assertEquals("manage pay", policy.id());
    }

    @Test
    public void updatePolicy() {
        final Policy policy = managePayPolicy();
        final ImmutablePolicy updated = ImmutablePolicy.copyOf(policy).withName("Foobar");
        backend.updatePolicy(updated);
        final Policy result = backend.getPolicy(updated.id());
        assertTrue(result.name().isPresent());
        assertEquals("Foobar", result.name().get());
    }

    @Test
    public void deletePolicy() {
        final String policyId = managePayPolicy().id();
        backend.deletePolicy(policyId);
        final Policy policy = backend.getPolicy(policyId);
        assertNull(policy);
    }

    @Test
    public void assignPolicy() {
        final String policyId = managePayPolicy().id();
        backend.assignPolicy(policyId, "john");
        final List<String> policyIds = backend.resolvePolicyIdsForPrincipal("john");
        assertTrue(policyIds.contains(policyId));
    }

    @Test
    public void unAssignPolicy() {
        final String policyId = managePayPolicy().id();
        backend.assignPolicy(policyId, "john");
        final List<String> policyIds = backend.resolvePolicyIdsForPrincipal("john");
        assertTrue(policyIds.contains(policyId));
        backend.unAssignPolicy(policyId, "john");
        final List<String> policyIds2 = backend.resolvePolicyIdsForPrincipal("john");
        assertFalse(policyIds2.contains(policyId));
    }

    @Test
    public void unAssignFromGroupShouldRemovePolicyAssignmentTransitively() {
        final ImmutableGroup allEmployees = allEmployees();
        final Group networkAdmins = networkAdmins();
        backend.createGroup(allEmployees);
        final Policy employeeReadOnlyPolicy = employeeReadOnlyPolicy();
        backend.createPolicy(employeeReadOnlyPolicy);
        backend.assignPolicy(employeeReadOnlyPolicy.id(), allEmployees.id());
        backend.assignPrincipalToGroup(allEmployees.id(), networkAdmins.id(), true);
        final String joe = "joe";
        backend.assignPrincipalToGroup(networkAdmins.id(), joe);
        final List<String> policyIds = backend.resolvePolicyIdsForPrincipal(joe);
        assertFalse(policyIds.isEmpty());
        assertEquals(2, policyIds.size());
        backend.unassignPrincipalFromGroup(networkAdmins.id(), joe);
        final List<String> policyIds2 = backend.resolvePolicyIdsForPrincipal(joe);
        assertTrue(policyIds2.isEmpty());
    }
}