package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableResourcePolicy.class)
@JsonDeserialize(builder = ImmutableResourcePolicy.Builder.class)
public interface ResourcePolicy {
    String resource();

    Optional<String> description();

    Optional<List<ResourcePolicyAssignment>> assignments();
}
