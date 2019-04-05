package com.erfangc.sac.core.service;

import com.erfangc.sac.interfaces.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

class PolicyDecisionMaker {

    private static String toRegex(String input) {
        return Stream
                .of(input.split("/"))
                .map(token -> {
                    if (token.equals("*")) {
                        return ".*";
                    } else {
                        return token;
                    }
                })
                .collect(joining("\\/"));
    }

    AuthorizationResponse makeAccessDecision(AuthorizationRequest request, List<Policy> policies) {
        boolean hasDeny = false;
        boolean hasPermit = false;
        for (Policy policy : policies) {
            if (policy.resource().isPresent()) {
                final String resource = policy.resource().get();
                final String regex = toRegex(resource);
                if (request.resource().matches(regex)) {
                    final Optional<List<String>> maybeActions = policy.actions();
                    if (maybeActions.isPresent()) {
                        final List<String> actions = maybeActions.get();
                        if (actions.contains(request.action()) || actions.contains("*")) {
                            if (policy.effectDeny().orElse(false).equals(true)) {
                                hasDeny = true;
                            } else {
                                hasPermit = true;
                            }
                        }
                    }
                }
            }
        }
        AuthorizationStatus status;
        if (hasPermit && !hasDeny) {
            status = AuthorizationStatus.Permitted;
        } else {
            status = AuthorizationStatus.Denied;
        }
        return ImmutableAuthorizationResponse.builder().status(status).requestId(request.id()).build();
    }
}
