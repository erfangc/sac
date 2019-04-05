package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAuthorizationResponse.class)
@JsonDeserialize(builder = ImmutableAuthorizationResponse.Builder.class)
public interface AuthorizationResponse {
    String requestId();

    AuthorizationStatus status();

    Optional<String> remarks();
}
