package com.erfangc.sac.core;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface Group {
    String id();

    String name();

    String description();

    List<GroupAssignment> assignments();
}
