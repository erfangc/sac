package com.erfangc.sac.core.service;

import com.erfangc.sac.core.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class PolicyDecisionMakerTest {

    @Test
    public void makeAccessDecision() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("increase")
                .build();

        List<Policy> policies = asList(
                ImmutablePolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Permitted, response.status());

    }

    @Test
    public void makeAccessDecisionEffectDeny() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("increase")
                .build();

        List<Policy> policies = asList(
                ImmutablePolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .effectDeny(true)
                        .build(),
                ImmutablePolicy
                        .builder()
                        .id("policy2")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Denied, response.status());

    }

    @Test
    public void makeAccessDecisionDeniedDueToLackOfExplicitPolicy() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/foobar")
                .action("increase")
                .build();

        List<Policy> policies = asList(
                ImmutablePolicy
                        .builder()
                        .id("policy2")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Denied, response.status());

    }

    @Test
    public void makeAccessDecisionMultiplePolicies() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/jack")
                .action("increase")
                .build();

        List<Policy> policies = asList(
                ImmutablePolicy
                        .builder()
                        .id("policy1")
                        .actions(Collections.singletonList("*"))
                        .resource("/hr/birthdays/*")
                        .build(),
                ImmutablePolicy
                        .builder()
                        .id("policy2")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/jack")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Permitted, response.status());

    }

    @Test
    public void makeAccessDecisionWildCard() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("increase")
                .build();

        List<Policy> policies = asList(
                ImmutablePolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/*")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Permitted, response.status());

    }

    @Test
    public void makeAccessDecisionArbitrarilyPlacedWildCard() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("increase")
                .build();

        List<Policy> policies = asList(
                ImmutablePolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/*/john")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Permitted, response.status());

    }
}