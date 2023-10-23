# 15. Project Structure

Date: 2023-10-23

## Decision

The overall project will consist of 4+ subprojects.

- `app` - The entry point, where our HTTP or calling library is used, and plugins are initialized.
- `shared` - A shared project that is imported to every plugin and contains all the helper classes that may be used by
  a plugin.
- `etor` - The ETOR plugin.  In the future there may be more plugins that will be new sub-projects in of themselves (or
  completely different projects and repositories).
- `e2e` - Our end-to-end tests.

## Status

Accepted.

## Context

### `app` Subproject

The `app` subproject contains the entry point into the code.  This may be the `main` method or a special method if we're
plugging into a cloud service's function compute service.  This means that it is directly connected to the HTTP library
or whatever library we use that handles the incoming requests.  For example, Javalin or the Azure SDK.

Second, `app` bootstraps the plugins that are available and hooks them into the aforementioned HTTP/request library.

Lastly, `app` converts any live request into the form that the plugins accept.

### `shared` Subproject

The `shared` subproject specifies the interface for a plugin.  Any plugin written needs to import this subproject, so it
can adhere to the plugin interface.

Second, `shared` contains all the shared helper classes that may be used by a plugin.  Perhaps most importantly is
the `ApplicationContext`.  Other examples include the logger, JSON handling, and an HTTP client.  `shared` does _not_
contain anything specific to a plugin.

### `etor` Subproject

`etor` is a plugin subproject.  As mentioned previously, `etor` depends on the `shared` subproject, so it can adhere to
the plugin interface.  This subproject contains everything specific to the ETOR usecase such as parsing orders,
converting orders, sending orders, and handling results.

### `e2e` Subproject

The `e2e` subproject is all about end-to-end testing.  It tests the external interface of the application instead of
testing individual classes and methods.

This subproject must not depend on any of the other subprojects.  It is kept isolated on purpose so that the tests can't
be "poisoned" by the implementation.

### Related Issues

_None_.
