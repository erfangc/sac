package com.erfangc.sac.interfaces;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface Resource {
    String resource();
    List<String> actions();
}
