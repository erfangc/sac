package com.erfangc.sac.core.backend.inmemory;

import com.erfangc.sac.core.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
        backend.assignPrincipalToGroup(networkAdmins().id(), "john");
        final List<String> policyIds = backend.resolvePolicyIdsForPrincipal("john");
        assertEquals(asList("server login", "employee read only"), policyIds);
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
        backend.update(
                ImmutableGroup
                        .copyOf(group)
                        .withName("Foo")
        );
        final Group result = backend.getGroup(groupId);
        assertEquals("Foo", result.name());
    }

    @Test
    public void delete() {
        backend.delete(networkAdmins().id());
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

        backend.unassignPrincipalToGroup(group.id(), "john");
        final Group result2 = backend.getGroup(group.id());
        final Optional<List<GroupAssignment>> assignments2 = result2.assignments();
        assertTrue(assignments2.isPresent());
        final Optional<GroupAssignment> john2 = assignments.get().stream().filter(r -> r.principal().equals("john")).findFirst();
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
        assertEquals("newPolicy", policy.id());
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
        backend.delete(policyId);
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

        backend.unassignPrincipalToGroup(policyId, "john");

        final List<String> policyIds2 = backend.resolvePolicyIdsForPrincipal("john");
        assertFalse(policyIds2.contains(policyId));
    }
}