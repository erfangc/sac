# Attribute forwarding

## Example

### The setup

Suppose we have a micro service that exposes an action "search" against a product catalogue. Further suppose this search functionality is powered by a search index like Apache Lucene or an OLAP engine. Finally, we assume this service is only accessible through an API Gateway which hosts or is otherwise responsible the authorization decision

### The access control problem

We want our service to allow anyone to "search" our catalogue except a portion of them labeled as "premium members only". These content are for registered users only. Clearly, in this case we would not want our authorization mechanism to "deny" access to users so access must be granted. However, to assist our service down-stream with potentially applying a filter based on the authenticated principal we may want to forward some attributes about that principal, such as the fact the user is not registered or isn't a premium member

## Principal attribute sourcing

Principal attributes are arbitrary key value pairs about a principal: such as age, profession, title, home country etc. Principal attributes can be sourced internally. In this case, one needs to invoke the `SAC` APIs to create and update `principal` attributes

```java
sac.setPrincipalAttribute("john", "title", "Sales Lead");
sac.setPrincipalAttribute("john", "region", "North America");
``` 

## Attribute forwarding in policies

```yaml
id: my policy
resource: /org/resources/some-resource
actions:
 - read
forward-attributes:
 - name
 - region
```

In this case, `sac.authorize()` will return a `AuthorizationResponse` that also contains a list of forward attributes:

```yaml
status: Permitted
attributes:
- key: title
  value: Sales Lead
- key: region
  value: North America
```

This information can then be forwarded to the downstream service to aid it's internal logic 
