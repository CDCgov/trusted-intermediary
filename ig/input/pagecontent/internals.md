### Overview

The ReportStream Intermediary is microservice behind the larger ReportStream system.  This page documents some of the internals behind the microservice.

### GitHub

We develop the ReportStream Intermediary on GitHub in an [opensource repository](https://github.com/CDCgov/trusted-intermediary).
Please feel free to peruse our code and contribute.

### Internal API

The ReportStream Intermediary has its own internal API that can be called by specific, trusted clients.

One can view the OpenAPI documentation by either looking at the [different YAML files in our repository](https://github.com/CDCgov/trusted-intermediary/tree/main/app/src/main/resources)
or run our application locally and hit the [OpenAPI endpoint](http://localhost:8080/openapi) to get a combined view of all the documentation.
In the future, we may have a UI set-up to more easily interact with the documentation.
