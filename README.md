# Simple Access Control (SAC)
[![CircleCI](https://circleci.com/gh/erfangc/sac/tree/master.svg?style=svg)](https://circleci.com/gh/erfangc/sac/tree/master)
[![codecov](https://codecov.io/gh/erfangc/sac/branch/master/graph/badge.svg)](https://codecov.io/gh/erfangc/sac)

Simple Access Control (SAC) is a friendly and streamlined authorization library inspired by industry standards such as IAM, ABAC (Attribute Based Access Control) and RBAC (Role Based Access Control). SAC borrows philosophically from XACML but replaces its OASIS specification with a modern JSON/YAML representation. Much of the more esoteric aspects are also ignored at the present moment in favor of simplicity and common use

## Demo
![demo](./docs/images/manufacturing-demo.gif)

## Difference from OAuth 2.0

The reason I felt the need to create this library instead of relying on OAuth 2.0 is mostly because OAuth 2.0 is built to solve for different needs. OAuth 2.0 assumes users are the owner of resources. Users in OAuth grants permissions to applications to access their data. While this is definitely true in social networking websites, this paradigm is in stark contrast to many use cases in enterprise, where resources are "administered" by an authority and various users are entitled to work with a sub-set of all available resources on the system. In the latter context, granular access control must be possible for an organization to function properly. This granularity must be achieved through a DSL that is expressive enough to cover a wide range of use cases and resource sharing schemes that OAuth simply was not designed to accommodate.

## Main Concepts

 > Note: identities are _not_ managed by SAC, we assume the principal under test is already authenticated and extracted from the upstream request. This lack of authentication assumption makes the authorization layer compatible with SAML, OIDC or custom authentication schemes

### Principal

A principal is the user or service user who is requesting authorization to perform an action against some resource. Note the principal's identity must be a unique property of the authentication process. For example, if we are using OIDC as the authentication scheme, then the JWT token's `sub` claim can be used as the principal

### Groups

Groups organize a collection of principals into a single entity. Authorization decisions that apply to individual principals can be applied to groups. When individual users are naturally or logically organized into domain specific entities such as departments, roles or organizations, we can create groups to bucket users (principals) along these demarcations and apply authorization policies uniformly. Groups simplify the administration of large numbers of users / principals

### Group Assignments

A group assignment is a `principal`, `group` pair that denotes the inclusion of the principal to the group. Principals can be assigned to or unassigned from groups. For example if user `james` is removed from the `network admin` group then any permissions associated with `network admin` will be lost to `james`

### Resource 

A `resource` is any entity on your network you wish to protect, such as user credentials, shopping carts, investment portfolios etc. Resources are denoted using the [Universal Resource Locator (URL)](https://en.wikipedia.org/wiki/URL) format. You (the consumer of SAC) have the freedom to organize your resources according to whatever hierarchy you deem fit. For example if you are running an online shopping website, you may use the scheme `/shopping-carts/user-id/shopping-cart-1` to convey the fact that shopping carts are segregated by user id

Note that resources are purely represented as strings and interpreted as `/` separated at runtime. SAC does not actually care if the resource exist on your systems or not, nor is SAC knowledgeable of the services that manage these resources. Further note that the URL hierarchy you use to describe and identify resources do not have to correspond to actual URLs you may ultimately expose in your RESTful APIs, though it is recommended that you keep them consistent

Finally and to summarize: a `resource` is any protected entity represented as a string. A resource is organized and identified via the `/` character. Conventionally, a resource must be a noun

### Policy

 >There are actually two flavors of policies: `ResourcePolicy` and `IdentityPolicy`. As a gentle introduction, please only consider `IdentityPolicy` for the rest of this README. The difference between the two types of policies are covered in [Resource vs identity policies](./docs/resource-vs-identity-policies.md).

The `Policy` (also known as an `IdentityPolicy`) is the primary object in SAC that describes authorization rules. A `policy` can be applied (assigned) to principals or groups. Policies defines the resources that the assigned principal have access to as well as the actions that principal is permitted to perform

Example policy to protect a single resource:
```yaml
id: secret key policy
resource: /secrets/secretKey
actions:
  - view
  - remove
  - alter
```

This example policy can be verbalized into natural language as follows:

 > The assignee of this policy may view, remove or alter the contents of the resource 
 identified as /secrets/secretKey

You may also use wildcards in policies to give broad permission:

```yaml
id: inventory manager policy
resource: /inventory/*
actions:
  - restock
  - sell
  - purge
```

#### Actions

`Actions` are verbs that a principal can perform against a resource. For example, if you are drafting a policy that is intended to grant read access to a certain document you will specify the action "read" as follows:

```yaml
id: my-document.txt readonly policy
resource: /documents/my-document.txt
actions:
  - read
```

Like resources, a wildcard can be used to represent all actions are allowed:

```yaml
id: my-document.txt
resource: /documents/my-document.txt
actions:
  - *
```

### Policy Assignment

Similar to `group assignment`, a `policy assignment` denote the applicability of a principal to a given policy. Creating a policy by itself does not actually affect any authorization SAC performs until that policy is assigned to an principal

## Usage

### Backends

In order to use the core functionalities of the library. You should first choose an appropriate `Backend`, which handles persistence and lifecycle of the concepts illustrates above, namely: `IdentityPolicy`, `ResourcePolicy`, `Group` etc. The `Backend` exposes a specific set of methods that an given storage medium must be able tp implement and support

```java
SimpleAccessControl sac = new SimpleAccessControlImpl(myBackend);
```

### Test Drive Using the In Memory Implementation

You can try out the functionalities of the core library without a durable back end like Postgres, MySQL or Redis. We provide an in-memory implementation of the core library that can be instantiated as follows: 

```java
final InMemorySimpleAccessControl sac = InMemorySimpleAccessControl.getInstance();
```

### Managing Groups

Groups are represented the with `Group` object. You can also attach assignments to the `Group` object you create. Construct one by using the immutable class as follows:

#### Creating / updating / deleting groups

```java
final String id = "network admins";
ImmutableGroup
        .builder()
        .addAssignments(
                ImmutableGroupAssignment
                        .builder()
                        .groupId(id)
                        .principal("johndoe")
                        .build(),
                ImmutableGroupAssignment
                        .builder()
                        .groupId(id)
                        .principal("unix network admins")
                        .principalIsGroup(true)
                        .build()
        )
        .id(id)
        .description("Esteemed colleagues from the network administration team")
        .name("Network Admins")
        .build();

// create a group
sac.createGroup(group);

// update a group
sac.update(group);

// delete a group
sac.delete(groupId);
```

#### Assigning / un-assigning principals

```java
String groupId = "human resources";

// assign a new principal to groups
sac.assignPrincipalToGroup(groupId, "johndoe");
sac.assignPrincipalToGroup(groupId, "janesmith");
sac.assignPrincipalToGroup(groupId, "external hr services", true);

// unassign
sac.unassignPrincipalToGroup(groupId, "johndoe");
```

#### Listing all the principals in a group

```java
List<String> principals = sac.getAllPrincipalsForGroup(groupId);
```

#### Group tree

Since group membership can consist of other groups, a given principal can be assigned to another group transitively. For example if `user1` is assigned to `group 1` and `group 1` is a member of `group 2` then `user1` is also a member of `group 2`. Any policies assigned to `group 2` will also be in effect for `user1`. You can query the membership, including transitive member ship, of a principal as follows:

#### Find all groups a given user belongs to

```java
List<Group> groups = sac.getGroupMembership("user1");
```

You can also graph the entire tree starting at a given group as well:

```java
Node root = sac.getGroupTree("my group");
```

#### Dealing with circular memberships

If `group1` is a member of `group2`, `group2` is a member of `group3` then neither `group1` or `group2` cannot be a a member of `group3` 

```
Assuming the setup:
Group1 -> Group2 -> Group3
Then the assignments:
Group2 -> Group1 is not allowed
Group2 -> Group3 is not allowed
```

### Managing Policies

#### Creating / updating / deleting policies

```java
final ImmutableIdentityPolicy policy = ImmutableIdentityPolicy
                .builder()
                .id("xyz")
                .resource("/my-org/my-dept/my-resource")
                .actions(Arrays.asList("read", "write"))
                .build();

// create a policy
sac.createPolicy(policy);

// update a policy
sac.updatePolicy(policy);

// delete a policy
sac.deletePolicy(policy.id());
```

#### Assign / un-assigning policy to principal or groups

```java
// assign a principal to policy
sac.assignPolicy(policyId, "user1");
sac.assignPolicy(policyId, "group1");

// unassign a principal to policy
sac.unAssignPolicy(policyId, "user1");
sac.unAssignPolicy(policyId, "group1");
```

### Making Authorization Decisions

#### Authorize an Action

The `AuthorizationRequest` object

```yaml
id: "123"
principal: "joe"
resource: "/org/dept/my-resource"
action: "read"
```

To make an authorization request using the core library, invoke the `authorize()` method

```java
final ImmutableAuthorizationRequest request = ImmutableAuthorizationRequest
                .builder()
                .action("read")
                .resource("/org/dept/my-resource")
                .id("123")
                .principal("joe")
                .build();
AuthorizationResponse resp = sac.authorize(request);
assert resp.status().equals(AuthorizationStatus.Permitted)
```

The `AuthorizationResponse` object:
```yaml
requestId: "123"
status: "Permitted"
remarks:
  present: true
```

## Development

### Components

The project is split into a few components for modularity. Each component is represented by a Maven module

### Modules

#### sac-core

`sac-core` represents the core module that handles the business logic of making the authorization decision. `sac-core` relies on data presented to it by other modules. The way `sac-core` interfaces with other modules is by declaring interfaces that the supplementary modules must implement 

#### sac-backend-redis

`sac-backend-redis` implements the `Backend` interface declared by `sac-core` using [Lettuce](https://github.com/lettuce-io/lettuce-core)

### Tests

All `Backend` and `SimpleAccessControl` implementations (ex: `RedisBackend`, `InMemoryBackend`) must pass a uniform set of tests at a minimum to guarantee operational readiness. These tests are represented as base classes in the `sac-backend-tests` module. This module offer base classes that you can extend to automatically acquire these tests (which are written in JUnit). These tests are:

 - Unit tests in `BackendTestBase`
 - A stateful test `StatefulTestBase`
 
The `StatefulTestBase` tests a comprehensive set of interactions layered on top of each other in an interactive scenario rather than testing only a single aspect of the system in isolation 

## TODOs

 - Terraform templates
 - Principal attribute forwarding in policies
 - Conditions in policies