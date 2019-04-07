package com.erfangc.sac.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttribute.class)
@JsonDeserialize(builder = ImmutableAttribute.Builder.class)
public interface Attribute {
    String key();

    String value();
}
