package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableIdentityPolicy.class)
@JsonDeserialize(builder = ImmutableIdentityPolicy.Builder.class)
public interface IdentityPolicy {
    String id();

    /**
     * When processing a chain of policies, any explicit denies overrides all
     * permits
     */
    Optional<Boolean> effectDeny();

    /**
     * Policy name
     */
    Optional<String> name();

    Optional<String> description();

    /**
     * Either the resource URL must be populated for a given policies
     * or the resources property must be populated
     */
    Optional<String> resource();

    /**
     * The actions that this policy allows (or denies)
     */
    Optional<List<String>> actions();

    /**
     * Either the resource URL must be populated for a given policies
     * or the resources property must be populated
     */
    Optional<List<Resource>> resources();
}
