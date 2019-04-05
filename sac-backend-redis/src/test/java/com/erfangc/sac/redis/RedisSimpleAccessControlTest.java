package com.erfangc.sac.redis;

import com.erfangc.sac.core.*;
import com.erfangc.sac.core.service.SimpleAccessControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

public class RedisSimpleAccessControlTest {

    private SimpleAccessControl sac;
    private RedisServer redisServer;

    @After
    public void tearDown() {
        redisServer.stop();
        System.clearProperty("sac.redis.endpoint");
    }

    @Before
    public void setUp() throws IOException {
        redisServer = new RedisServer(8080);
        redisServer.start();
        System.setProperty("sac.redis.endpoint", "localhost:8080");
        sac = RedisSimpleAccessControl.getInstance();
        final Group networkAdmins = networkAdmins();
        sac.createGroup(networkAdmins);
        final Group humanResources = humanResources();
        sac.createGroup(humanResources);
        final Policy employeeReadOnlyPolicy = employeeReadOnlyPolicy();
        sac.createPolicy(employeeReadOnlyPolicy);
        final Policy serverLoginPolicy = serverLoginPolicy();
        sac.createPolicy(serverLoginPolicy);
        final Policy managePayPolicy = managePayPolicy();
        sac.createPolicy(managePayPolicy);
        sac.assignPolicy(employeeReadOnlyPolicy.id(), networkAdmins.id());
        sac.assignPolicy(serverLoginPolicy.id(), networkAdmins.id());
        sac.assignPolicy(employeeReadOnlyPolicy.id(), humanResources.id());
        sac.assignPolicy(managePayPolicy.id(), humanResources.id());
    }

    private ImmutableGroup allEmployees() {
        return ImmutableGroup.
                builder()
                .id("all employees")
                .name("All Employees")
                .build();
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
    public void createGroup() {
        final ImmutableGroup group = ImmutableGroup.builder().id("new group").name("New Group").build();
        sac.createGroup(group);
        final Group result = sac.getGroup(group.id());
        assertEquals(group.id(), result.id());
    }

    @Test
    public void getGroup() {
        final Group group = sac.getGroup(networkAdmins().id());
        assertEquals("Network Administrators", group.name());
    }

    @Test
    public void update() {
        final String groupId = networkAdmins().id();
        final Group group = sac.getGroup(groupId);
        sac.updateGroup(
                ImmutableGroup
                        .copyOf(group)
                        .withName("Foo")
        );
        final Group result = sac.getGroup(groupId);
        assertEquals("Foo", result.name());
    }

    @Test
    public void deleteGroup() {
        sac.deleteGroup(networkAdmins().id());
        final Group group = sac.getGroup(networkAdmins().id());
        assertNull(group);
    }

    @Test
    public void assignPrincipalToGroup() {
        final Group group = networkAdmins();
        sac.assignPrincipalToGroup(group.id(), "john");
        final Group result = sac.getGroup(group.id());
        final Optional<List<GroupAssignment>> assignments = result.assignments();
        assertTrue(assignments.isPresent());
        final Optional<GroupAssignment> john = assignments.get().stream().filter(r -> r.principal().equals("john")).findFirst();
        assertTrue(john.isPresent());
    }

    @Test
    public void unassignPrincipalToGroup() {
        final Group group = networkAdmins();
        sac.assignPrincipalToGroup(group.id(), "john");
        final Group result = sac.getGroup(group.id());
        final Optional<List<GroupAssignment>> assignments = result.assignments();
        assertTrue(assignments.isPresent());
        final Optional<GroupAssignment> john = assignments.get().stream().filter(r -> r.principal().equals("john")).findFirst();
        assertTrue(john.isPresent());
        sac.unassignPrincipalFromGroup(group.id(), "john");
        final Group result2 = sac.getGroup(group.id());
        final Optional<List<GroupAssignment>> assignments2 = result2.assignments();
        assertTrue(assignments2.isPresent());
        final Optional<GroupAssignment> john2 = assignments2.get().stream().filter(r -> r.principal().equals("john")).findFirst();
        assertFalse(john2.isPresent());
    }

    @Test
    public void getAllPrincipalsForGroup() {
        final Group group = networkAdmins();
        sac.assignPrincipalToGroup(group.id(), "john");
        sac.assignPrincipalToGroup(group.id(), "jsmith");
        final List<String> principals = sac.getAllPrincipalsForGroup(group.id());
        assertTrue(principals.contains("john"));
        assertTrue(principals.contains("jsmith"));
    }

    @Test
    public void getGroupMembership() {
        final Group networkAdmins = networkAdmins();
        final Group hr = humanResources();
        sac.assignPrincipalToGroup(networkAdmins.id(), "john");
        sac.assignPrincipalToGroup(hr.id(), "john");
        final List<String> groups = sac.getGroupMembership("john");
        assertEquals(2, groups.size());
        assertTrue(groups.contains(networkAdmins.id()));
        assertTrue(groups.contains(hr.id()));
    }

    @Test
    public void createPolicy() {
        Policy newPolicy = ImmutablePolicy
                .builder()
                .id("new policy")
                .resource("/foo/bar")
                .actions(singletonList("*"))
                .build();
        sac.createPolicy(newPolicy);
        final Policy policy = sac.getPolicy(newPolicy.id());
        assertEquals("new policy", policy.id());
    }

    @Test
    public void getPolicy() {
        final Policy policy = sac.getPolicy(managePayPolicy().id());
        assertEquals("manage pay", policy.id());
    }

    @Test
    public void updatePolicy() {
        final Policy policy = managePayPolicy();
        final ImmutablePolicy updated = ImmutablePolicy.copyOf(policy).withName("Foobar");
        sac.updatePolicy(updated);
        final Policy result = sac.getPolicy(updated.id());
        assertTrue(result.name().isPresent());
        assertEquals("Foobar", result.name().get());
    }

    @Test
    public void deletePolicy() {
        final String policyId = managePayPolicy().id();
        sac.deletePolicy(policyId);
        final Policy policy = sac.getPolicy(policyId);
        assertNull(policy);
    }

    @Test
    public void authorizeHrToManagePay() {
        final Group humanResources = humanResources();
        final Group networkAdmins = networkAdmins();
        final String hrGuy = "hr guy";
        final String itGuy = "it guy";
        sac.assignPrincipalToGroup(humanResources.id(), hrGuy);
        sac.assignPrincipalToGroup(networkAdmins.id(), itGuy);

        final ImmutableAuthorizationRequest authorizationRequest = ImmutableAuthorizationRequest
                .builder()
                .id("test request")
                .action("increase")
                .principal(hrGuy)
                .resource("/org/employees/jsmith/pay")
                .build();
        final AuthorizationResponse authorizationResponse = sac.authorize(authorizationRequest);

        assertEquals(AuthorizationStatus.Permitted, authorizationResponse.status());

        // hrGuy can no longer change pay after he has been removed from group
        sac.unassignPrincipalFromGroup(humanResources.id(), hrGuy);

        final AuthorizationResponse authorizationResponse1 = sac.authorize(authorizationRequest);
        assertEquals(AuthorizationStatus.Denied, authorizationResponse1.status());

        // itGuy shouldn't be able to manage pay either
        final AuthorizationResponse authorizationResponse2 = sac.authorize(authorizationRequest.withPrincipal(itGuy));
        assertEquals(AuthorizationStatus.Denied, authorizationResponse2.status());
    }

    @Test
    public void authorizeActionsTransitively() {
        final Group humanResources = humanResources();
        final Group networkAdmins = networkAdmins();
        final ImmutableGroup allEmployees = allEmployees();
        sac.createGroup(allEmployees);
        sac.assignPrincipalToGroup(allEmployees.id(), humanResources.id(), true);
        sac.assignPrincipalToGroup(allEmployees.id(), networkAdmins.id(), true);
        final String hrGuy = "hr guy";
        final String itGuy = "it guy";
        sac.assignPrincipalToGroup(humanResources.id(), hrGuy);
        sac.assignPrincipalToGroup(networkAdmins.id(), itGuy);

        final Policy employeeReadOnlyPolicy = employeeReadOnlyPolicy();
        sac.createPolicy(employeeReadOnlyPolicy);
        sac.assignPolicy(employeeReadOnlyPolicy.id(), allEmployees.id());

        // both itGuy and hrGuy should be able to access an employee record
        final ImmutableAuthorizationRequest authorizationRequest = ImmutableAuthorizationRequest
                .builder()
                .id("test request")
                .action("read")
                .principal(hrGuy)
                .resource("/org/employees/jackjones")
                .build();
        final AuthorizationResponse authorizationResponse = sac.authorize(authorizationRequest);
        assertEquals(AuthorizationStatus.Permitted, authorizationResponse.status());

        final AuthorizationResponse authorizationResponse1 = sac.authorize(authorizationRequest.withPrincipal(itGuy));
        assertEquals(AuthorizationStatus.Permitted, authorizationResponse1.status());

        // but both still cannot perform a function that is not in their job role
        final AuthorizationResponse authorizationResponse2 = sac.authorize(authorizationRequest.withAction("delete"));
        assertEquals(AuthorizationStatus.Denied, authorizationResponse2.status());
    }
}