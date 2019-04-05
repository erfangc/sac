# Resource vs. principal based policy assignment

In a typical scenario for an authorization problem: policies are designed to project resources on the system by explicitly granting action verbs that the holder of the policy can perform against a set of given resources. Any principal can be assigned multiple policies

This relationship, hence forth denoted as principal based policy assignment, can be illustrated as follows:

![](principal-policy-assignment.svg)

However, if there are tens of thousands of or even millions of resources that must be protected, and users can be entitled to thousands of these resources at a time, then assigning thousands of policies to individual principals become untenable. This is a problem made worse by the fact that if a principal has thousands of policies assigned, then each access decision needs to iterate through all of those policies regardless of their applicability to the current request. 

Therefore, it is better in those scenarios to invert the representation and store a policy keyed on the resource it protect. We define this approach as resource based policy assignment and can be illustrated as follows:

![](resource-policy-assignment.svg)

Note that since in the resource based policy assignment, the resource is the primary key of the policy, you're not allowed to create multiple resource based policies against the same resource. You can still use the former approach in-conjunction to achieve granular access control