package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableGroup.class)
@JsonDeserialize(builder = ImmutableGroup.Builder.class)
public interface Group {
    String id();

    String name();

    Optional<String> description();

    Optional<List<GroupAssignment>> assignments();
}
