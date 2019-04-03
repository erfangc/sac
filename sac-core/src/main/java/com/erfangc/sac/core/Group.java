package com.erfangc.sac.core;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface Group {
    String id();

    String name();

    Optional<String> description();

    Optional<List<GroupAssignment>> assignments();
}
