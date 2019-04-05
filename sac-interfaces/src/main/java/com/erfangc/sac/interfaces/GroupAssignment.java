package com.erfangc.sac.interfaces;

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
