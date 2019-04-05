package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAuthorizationRequest.class)
@JsonDeserialize(builder = ImmutableAuthorizationRequest.Builder.class)
public interface AuthorizationRequest {
    String id();

    String principal();

    String resource();

    String action();
}
