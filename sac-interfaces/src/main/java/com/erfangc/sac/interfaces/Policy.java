package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePolicy.class)
@JsonDeserialize(builder = ImmutablePolicy.Builder.class)
public interface Policy {
    String id();

    Optional<Boolean> effectDeny();

    Optional<String> name();

    Optional<String> description();

    Optional<String> resource();

    Optional<List<String>> actions();

    Optional<List<Resource>> resources();
}
