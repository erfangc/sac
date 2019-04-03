package com.erfangc.sac.core.backend.inmemory;

import com.erfangc.sac.core.Policy;

class PolicyAttachment {
    private final String id;
    private final Policy policy;
    private final String principal;

    public PolicyAttachment(String id, Policy policy, String principal) {
        this.id = id;
        this.policy = policy;
        this.principal = principal;
    }

    public String getId() {
        return id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public String getPrincipal() {
        return principal;
    }
}
