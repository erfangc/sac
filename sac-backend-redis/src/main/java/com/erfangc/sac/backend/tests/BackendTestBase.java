package com.erfangc.sac.backend.tests;

import com.erfangc.sac.core.service.SimpleAccessControl;
import com.erfangc.sac.interfaces.Group;
import com.erfangc.sac.interfaces.ImmutableGroup;
import com.erfangc.sac.interfaces.ImmutablePolicy;
import com.erfangc.sac.interfaces.Policy;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class BackendTestBase {

    protected SimpleAccessControl sac;

    protected Group allEmployees() {
        return ImmutableGroup.
                builder()
                .id("all employees")
                .name("All Employees")
                .build();
    }

    protected Policy managePayPolicy() {
        return ImmutablePolicy
                .builder()
                .id("manage pay")
                .actions(asList("increase", "decrease"))
                .resource("/org/employees/*/pay")
                .build();
    }

    protected Policy serverLoginPolicy() {
        return ImmutablePolicy
                .builder()
                .id("server login")
                .actions(singletonList("login"))
                .resource("/org/servers/*")
                .build();
    }

    protected Policy employeeReadOnlyPolicy() {
        return ImmutablePolicy
                .builder()
                .id("employee read only")
                .actions(singletonList("read"))
                .resource("/org/employees/*")
                .build();
    }

    protected Group humanResources() {
        return ImmutableGroup
                .builder()
                .name("Human Resources")
                .id("hr")
                .build();
    }

    protected Group networkAdmins() {
        return ImmutableGroup
                .builder()
                .name("Network Administrators")
                .id("network admins")
                .build();
    }

    protected void initializePolicyBackendStates() {
        final Group networkAdmins = networkAdmins();
        sac.createGroup(networkAdmins);

        final Group humanResources = humanResources();
        sac.createGroup(humanResources);

        final Group allEmployees = allEmployees();
        sac.createGroup(allEmployees);

        final Policy employeeReadOnlyPolicy = employeeReadOnlyPolicy();
        sac.createPolicy(employeeReadOnlyPolicy);
        sac.assignPolicy(employeeReadOnlyPolicy.id(), allEmployees.id());

        sac.assignPrincipalToGroup(allEmployees.id(), humanResources.id(), true);
        sac.assignPrincipalToGroup(allEmployees.id(), networkAdmins.id(), true);

        final Policy serverLoginPolicy = serverLoginPolicy();
        sac.createPolicy(serverLoginPolicy);
        sac.assignPolicy(serverLoginPolicy.id(), networkAdmins.id());

        final Policy managePayPolicy = managePayPolicy();
        sac.createPolicy(managePayPolicy);
        sac.assignPolicy(managePayPolicy.id(), humanResources.id());
    }

}
