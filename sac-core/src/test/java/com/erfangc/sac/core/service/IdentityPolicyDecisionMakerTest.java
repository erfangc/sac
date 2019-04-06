package com.erfangc.sac.core.service;

import com.erfangc.sac.interfaces.*;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class IdentityPolicyDecisionMakerTest {

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

        List<IdentityPolicy> policies = singletonList(
                ImmutableIdentityPolicy
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
    public void makeAccessDecisionMultipleDenyEffects() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("increase")
                .build();

        List<IdentityPolicy> policies = asList(
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .effectDeny(true)
                        .build(),
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy2")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .effectDeny(true)
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Denied, response.status());
    }

    @Test
    public void makeAccessDecisionAllPowerful() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("increase")
                .build();

        List<IdentityPolicy> policies = singletonList(
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy1")
                        .actions(singletonList("*"))
                        .resource("*")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Permitted, response.status());
    }

    @Test
    public void makeAccessDecisionActionWildcard() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        AuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("increase")
                .build();

        List<IdentityPolicy> policies = singletonList(
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy1")
                        .actions(singletonList("*"))
                        .resource("/hr/salaries/*")
                        .build()
        );

        final AuthorizationResponse response = policyDecisionMaker.makeAccessDecision(request, policies);
        assertEquals(AuthorizationStatus.Permitted, response.status());
    }

    @Test
    public void makeAccessDecisionDenyOnAction() {

        final PolicyDecisionMaker policyDecisionMaker = new PolicyDecisionMaker();
        final String requestId = UUID.randomUUID().toString();

        List<IdentityPolicy> policies = singletonList(
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .build()
        );

        AuthorizationRequest request2 = ImmutableAuthorizationRequest
                .builder()
                .id(requestId)
                .principal("john")
                .resource("/hr/salaries/john")
                .action("delete")
                .build();

        final AuthorizationResponse response2 = policyDecisionMaker.makeAccessDecision(request2, policies);
        assertEquals(AuthorizationStatus.Denied, response2.status());

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

        List<IdentityPolicy> policies = asList(
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .effectDeny(true)
                        .build(),
                ImmutableIdentityPolicy
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

        List<IdentityPolicy> policies = asList(
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy1")
                        .actions(asList("increase", "decrease"))
                        .resource("/hr/salaries/john")
                        .build(),
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy2")
                        .actions(singletonList("*"))
                        .resource("/hr/birthdays")
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

        List<IdentityPolicy> policies = asList(
                ImmutableIdentityPolicy
                        .builder()
                        .id("policy1")
                        .actions(singletonList("*"))
                        .resource("/hr/birthdays/*")
                        .build(),
                ImmutableIdentityPolicy
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

        List<IdentityPolicy> policies = singletonList(
                ImmutableIdentityPolicy
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

        List<IdentityPolicy> policies = singletonList(
                ImmutableIdentityPolicy
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