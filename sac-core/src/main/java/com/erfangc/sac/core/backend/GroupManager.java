package com.erfangc.sac.core.backend;

import com.erfangc.sac.interfaces.Group;
import com.erfangc.sac.interfaces.Node;

import java.util.List;

/**
 * {@link GroupManager} implementations manages the lifecycle, updates and interactions with {@link com.erfangc.sac.interfaces.Group}
 * instances. The implementation must provide for ways to traverse the graph formed from nesting {@link com.erfangc.sac.interfaces.Group}
 * to help with the authorization process
 */
public interface GroupManager {
    void createGroup(Group group);

    Group getGroup(String id);

    void updateGroup(Group group);

    void deleteGroup(String groupId);

    Node getGroupTree(String groupId);

    List<String> getAllPrincipalsForGroup(String groupId);

    List<String> getGroupMembership(String principalId);

    List<String> getGroupMembershipTransitively(String principalId);

    void assignPrincipalToGroup(String groupId, String principalId);

    void assignPrincipalToGroup(String groupId, String principalId, boolean principalIsGroup);

    void unassignPrincipalFromGroup(String groupId, String principalId);
}
