package com.erfangc.sac.core;

import org.immutables.value.Value;

@Value.Immutable
public interface GroupAssignment {
    String groupId();

    String principal();

    @Value.Default
    default boolean principalIsGroup() {
        return false;
    }
}
