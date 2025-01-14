# 27. Application Context Supporting Different Implementations Per Thread

Date: 2025-01-13

## Decision

One normally registers an implementation in the application context, and it stays that way for the entire execution of
the program.  We have added the ability for us to swap out different implementations on a thread-by-thread basis.

## Status

Accepted.

## Context

For load testing in a real environment, we decided that we didn't want to send that load testing data onto ReportStream,
but we also didn't want to do a special deploy or feature flag that would temporarily disable the sending of data to
ReportStream.  Instead, we wanted TI to be able to handle real data while also handling fake, load test data at the same
time.

Because each HTTP request is handled on its own thread, we are able to change the ReportStream client implementation
between the real one or a mock one depending on whether a load test HTTP request header is passed in or not.

A new method was added called `ApplicationContext#registerForThread`.

## Impact

### Positive

- We can now change implementations on an HTTP request-by-request (thread-by-thread) basis.
- We can load test TI without load testing RS while still handling legitimate traffic.

## Negative

- We can't use `@Inject` for classes that we decide to start swapping out.  Injecting registered implementations into
  `@Inject` fields only happens on bootstrap which is before any HTTP requests are handled.  Calling
  `ApplicationContext#injectRegisteredImplementations` after swapping out an implementation and hoping it works with
  `@Inject` will not work because our program is multithreaded and we use singletons.  So, that `@Inject` field is used
  by many HTTP requests, some of which may not want a swapped out implementation.  The solution to this is to use
  `ApplicationContext#getImplementation` in the method that requires the implementation.  For example, see
  `RSEndpointClient`; we never `@Inject` it anywhere now, but instead we use `ApplicationContext#getImplementation`.
- Specifically for the load testing, we need to remember to duplicate the logic that swaps out the ReportStream client
  implementation to any new HTTP endpoint that can call ReportStream.  If we forget, then we risk calling ReportStream a lot during load tests.

### Risks

- There are nuances to using this new mechanism of ApplicationContext that can lead to bugs.  For example, thinking you can still use `@Inject`.
- Specifically for the load testing, forgetting to duplicate the logic that swaps out the ReportStream client on a new
  HTTP endpoint that will call ReportStream.

## Related Issues

#1122.
