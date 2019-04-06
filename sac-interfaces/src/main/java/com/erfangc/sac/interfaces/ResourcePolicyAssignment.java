package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableResourcePolicyAssignment.class)
@JsonDeserialize(builder = ImmutableResourcePolicyAssignment.Builder.class)
public interface ResourcePolicyAssignment {
    String principal();

    List<String> actions();
}
