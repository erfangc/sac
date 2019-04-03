package com.erfangc.sac.core.backend.inmemory;

import com.erfangc.sac.core.Group;

class GroupMembership {
    private final String id;
    private final Group group;
    private final String principal;

    public GroupMembership(String id, Group group, String principal) {
        this.id = id;
        this.group = group;
        this.principal = principal;
    }

    public String getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public String getPrincipal() {
        return principal;
    }
}
