# Simple Access Control (SAC)

Simple Access Control (SAC) is a friendly and streamlined authorization library inspired by industry standards such as IAM, ABAC (Attribute Based Access Control) and RBAC (Role Based Access Control). SAC borrows philosophically from XACML but replaces its OASIS specification with a modern JSON/YAML representation. Much of the more esoteric aspects are also ignored at the present moment in favor of simplicity

## Main Concepts

 > Note: identities are _not_ managed by SAC, we assume the principal under test is already authenticated and extracted from the upstream request. This lack of authentication assumption makes the authorization layer compatible with SAML, OIDC or custom authentication schemes

### Principal

A principal is the user or service user who is requesting authorization to perform an action against some resource. Note the principal's identity must be a unique property of the authentication process. For example, if we are using OIDC as the authentication scheme, then the JWT token's `sub` claim can be used as the principal

### Groups

Groups organize a collection of principals into a single entity. Authorization decisions that apply to individual principals can be applied to groups. When individual users are naturally or logically organized into domain specific entities such as departments, roles or organizations, we can create groups to bucket users (principals) along these demarcations and apply authorization policies uniformly. Groups simplify the administration of large numbers of users / principals

### Group Assignments

A group assignment is a `principal`, `group` pair that denotes the inclusion of the principal to the group. Principals can be assigned to or unassigned from groups. For example if user `james` is removed from the `network admin` group then any permissions associated with `network admin` will be lost to `james` 

### Policy

The `policy` is the primary object in SAC that describes authorization rules. A `policy` can be applied (assigned) to principals or groups. The `policy` defines the resources that the assigned principal have access to as well as the actions that principal is permitted to perform

Example Policy:
```yaml
policy:
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

Multiple resources in a single policy can be expressed as:
```yaml
policy:
    id: friend policy
    resources:
      - /friends/friend1
      - /friends/friend2
    actions:
      - message
      - unfriend
``` 

You may also use wildcards in policies to give broad permission:

```yaml
policy
    id: inventory manager policy
    resource: /inventory/*
    actions:
      - restock
      - sell
      - purge
```

#### Resource

A `resource` is any entity on your network you wish to protect, such as user credentials, shopping carts, investment portfolios etc. Resources are denoted using the [Universal Resource Locator (URL)](https://en.wikipedia.org/wiki/URL) format. You (the consumer of SAC) have the freedom to organize your resources according to whatever hierarchy you deem fit. For example if you are running an online shopping website, you may use the scheme `/shopping-carts/user-id/shopping-cart-1` to convey the fact that shopping carts are segregated by user id

Note that resources are purely represented as strings and interpreted as `/` separated at runtime. SAC does not actually care if the resource exist on your systems or not, nor is SAC knowledgeable of the services that manage these resources. Further note that the URL hierarchy you use to describe and identify resources do not have to correspond to actual URLs you may ultimately expose in your RESTful APIs, though it is recommended that you keep them consistent

Finally and to summarize: a `resource` is any protected entity represented as a string. A resource is organized and identified via the `/` character. Conventionally, a resource must be a noun

#### Actions

`Actions` are verbs that a principal can perform against a resource. For example, if you are drafting a policy that is intended to grant read access to a certain document you will specify the action "read" as follows:

```yaml
policy:
    id: my-document.txt readonly policy
    resource: /documents/my-document.txt
    actions:
      - read
```

Like resources, a wildcard can be used to represent all actions are allowed

### Policy Assignment

Similar to `group assignment`, a `policy assignment` denote the applicability of a principal to a given policy. Creating a policy by itself does not actually affect any authorization SAC performs until that policy is assigned to an principal

## Use Cases

### Managing Groups

#### Creating / updating / deleting groups

#### Assigning / un-assigning principals

#### Listing all the users in a group

#### Find all groups a given user belongs to

### Managing Policies

#### Creating / updating / deleting policies

#### Assign / un-assigning policy to principal or groups

### Making Authorization Decisions

#### Authorize an Action

The `AuthorizationRequest` object

```yaml

```

```java

```

To make an authorization request using the core library, invoke the `authorize()` method

```java
AuthorizationResponse resp = sac.authorize(request);
assert resp.getStatus().equals(AuthorizationStatus.Permitted)
```

The `AuthorizationResponse` object

## Development

### Components

### Modules

#### sac-core

#### sac-store-h2