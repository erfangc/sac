package com.erfangc.sac.core.backend;

import com.erfangc.sac.interfaces.ResourcePolicy;

import java.util.Set;

/**
 * {@link ResourcePolicyManager} manages the lifecycle and persistence of {@link com.erfangc.sac.interfaces.ResourcePolicy} instances
 * on the system. The performance characteristics of creating, updating, querying specific resource's policy by id should not be a function
 * of the number of resource based policies on the system. This is the hallmark of resource policy's point for existence
 * <p>
 * In identity based policies, many policies are attached to identities and authorization decisions need to scan through all attached policies before
 * reaching an authorization conclusion. This is generally a bad practice if the # of policies can grow indefinitely as the # of resources on the systems
 * grow indefinitely. For those type resources that can grow indefinitely, it is preferred to use a resource based policy to ensure authorization can be
 * performed in O(1) time instead of O(N)
 */
public interface ResourcePolicyManager {

    /**
     * Grants the specified principal the set of specified actions against the given resource
     *
     * @param resource  the resource to grant access to
     * @param principal the principal who should be granted access
     * @param actions   the actions that the principal should be granted access to perform against the specified resource
     */
    void grantActions(String resource, String principal, Set<String> actions);

    /**
     * Revokes the specified principal from performing the set of specified actions against the given resource
     *
     * @param resource  the resource to revoke access to
     * @param principal the principal whose access to the resource should be revoked
     * @param actions   the actions that the principal should no longer be able to perform against the specified resource
     */
    void revokeActions(String resource, String principal, Set<String> actions);

    /**
     * Retrieve all policies associated with the given resource
     *
     * @param resource the resource identifier
     * @return a {@link ResourcePolicy}
     */
    ResourcePolicy getResourcePolicy(String resource);
}
