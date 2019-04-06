package com.erfangc.sac.interfaces;

import java.util.List;

/**
 * The main interface for performing access control functions
 */
public interface SimpleAccessControl {

    /**
     * Create a group and persist it
     *
     * @param group {@link Group}
     */
    void createGroup(Group group);

    /**
     * Retrieve a {@link Group} based on the specified Id
     *
     * @param id groupId
     * @return a {@link Group}
     */
    Group getGroup(String id);

    /**
     * Update the {@link Group} and replace it with a new one
     *
     * @param group the {@link Group} to be updated
     */
    void updateGroup(Group group);

    /**
     * Delete the given {@link Group} by its id
     *
     * @param groupId the groupId to delete
     */
    void deleteGroup(String groupId);

    /**
     * Assign the principalId provided to the given group
     *
     * @param groupId     the groupId to assign to
     * @param principalId the principalId to assign
     */
    void assignPrincipalToGroup(String groupId, String principalId);

    /**
     * Assign the principalId provided to the given group. The principalId can be
     * a groupId as well
     *
     * @param groupId          the groupId to assign to
     * @param principalId      the principalId to assign (or a groupId for nesting groups)
     * @param principalIsGroup denote the principal being assigned is a group
     */
    void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup);

    /**
     * Unassign the principalId from the group
     *
     * @param groupId     the groupId to unassign from
     * @param principalId the principalId to unassign
     */
    void unassignPrincipalFromGroup(String groupId, String principalId);

    /**
     * Get all the principals from a given group
     *
     * @param groupId the groupId to retrieve principals for
     * @return a list of principals
     */
    List<String> getAllPrincipalsForGroup(String groupId);

    /**
     * For a given principal, retrieve all the groups it belongs to. This method is not transitive, and
     * should only return immediate memberships
     *
     * @param principalId the principalId to retrieve group membership for
     * @return a list of groupIds representing all groups this principal is a member of
     */
    List<String> getGroupMembership(String principalId);

    /**
     * For a given principal, retrieve all groups the principal belongs, then traverse
     * any of those groups that may be nested, so and so forth and return a flattened list
     *
     * @param principalId the principalId to retrieve membership for
     * @return a flattened list of group membership including those that are assigned transitively (i.e. nested)
     */
    List<String> getGroupMembershipTransitively(String principalId);

    /**
     * Return the graph of connected groups given a root group
     *
     * @param groupId the root groupId
     * @return a {@link Node} which points to other connected groups from the root group
     */
    Node getGroupTree(String groupId);

    /**
     * Create a {@link IdentityPolicy} and persist it
     *
     * @param identityPolicy the {@link IdentityPolicy} to create and persist
     */
    void createPolicy(IdentityPolicy identityPolicy);

    /**
     * Retrieve a {@link IdentityPolicy} by its id
     *
     * @param policyId the policyId to retrieve
     * @return a given {@link IdentityPolicy}
     */
    IdentityPolicy getPolicy(String policyId);

    /**
     * Updates a {@link IdentityPolicy}
     *
     * @param identityPolicy the policy to be updated
     */
    void updatePolicy(IdentityPolicy identityPolicy);

    /**
     * Delete a policy by it's id
     *
     * @param policyId the policyId to delete
     */
    void deletePolicy(String policyId);

    void assignPolicy(String policyId, String principalId);

    void unAssignPolicy(String policyId, String principalId);

    /**
     * The primary method that handles authorization tasks by accepting a {@link AuthorizationRequest} and producing a {@link AuthorizationResponse}
     *
     * @param request the {@link AuthorizationRequest} object
     * @return a {@link AuthorizationResponse}
     */
    AuthorizationResponse authorize(AuthorizationRequest request);

}
