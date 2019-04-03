package com.erfangc.sac.core;

import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
public interface Policy {
    String id();

    String name();

    String description();

    Optional<String> resource();

    Optional<List<String>> actions();

    Optional<List<Resource>> resources();
}
