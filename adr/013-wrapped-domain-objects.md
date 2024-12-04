# 13. Wrapped Domain Objects

Date: 2023-06-19

## Decision

We will "wrap" the HAPI FHIR (or any other FHIR library we may pivot to) resource classes with an interface to prevent the HAPI library from "polluting" our business logic.

## Status

Accepted.

## Context

Per [ADR 5](./005-oea.md), one of the principles to follow is that less important code should depend upon more important code.  Third party dependencies are less important than our business logic, so how do we isolate the HAPI library's resource classes from our business logic?

We typically isolate libraries by creating an Interface at the library boundary.  To take advantage of Java's type safety we define the interface using Generic types to ensure that the correct classes are used.  On a wrapper Interface we always include a method to retrieve the underlying object.  Extra methods are defined based on business needs.  The implementation of this interface is customized to use the underlying third party library.

Our business logic can then use the wrapper Interface without needing to know the actual underlying implementation, and that implementation can be changed without changing the Interface that the business logic relies on.

You can see an example of this in [Order.java](../etor/src/main/java/gov/hhs/cdc/trustedintermediary/etor/orders/Order.java) (the interface) and [HapiOrder.java](../etor/src/main/java/gov/hhs/cdc/trustedintermediary/external/hapi/HapiOrder.java) (the implementation).


## Impact

### Positive

- **Decoupling of Business Logic and Third-Party Libraries:** allows us to decouple our business logic from the specific implementation details of the third-party library. This enhances maintainability and readability.
  

- **Flexibility and Extensibility:** The use of interfaces provides flexibility to switch to a different FHIR library in the future without requiring significant changes to the business logic. New functionalities can also be added easily by extending the wrapper interface.

  
- **Enhanced Testability:** The wrapper interface makes it easier to mock (or stub) dependencies during testing, allowing for more effective unit tests without relying on the actual HAPI FHIR implementation.
  

- **Consistent API:** Wrapping the resource classes can help enforce a consistent API across different types of resources.

### Negative

- **Performance Overhead:** Wrapping resource classes could introduce slight performance overhead due to additional method calls. However, this is generally minimal and acceptable for the benefits gained.
  

- **Maintenance of Wrapper Classes:** As the HAPI FHIR library or business requirements evolve, the wrapper classes will need to be maintained to ensure they remain compatible with the underlying library.

### Risks

- **Increased Complexity:** Having an additional layer (the wrapper) may add complexity to the codebase. Developers must understand both the wrapper and the underlying library, which can increase the learning curve.
  

- **Dependency on Wrapper Design:** If the wrapper interface is not designed thoughtfully, it may limit flexibility or create tight coupling with specific implementations, countering the goal of decoupling.
  

- **Integration Challenges:** If we switch to a different FHIR library, there may be unforeseen integration challenges that arise from differences in implementation details or API design.
  

- **Increased Development Time:** Initially creating wrapper classes may require additional development time and effort, especially if the resource classes are extensive or complex.


### Related Issues

- #79
- [ADR 005 - Option-enabling Architecture (OeA)](./005-oea.md)

### Additional References

- [Flexion Case Study](https://flexion.us/ustc-dawson-case-study/)
