package gov.hhs.cdc.trustedintermediary.domainconnector;

import java.util.Objects;

/** Specifies an HTTP endpoint. */
public record HttpEndpoint(String verb, String path, boolean isProtected) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof final HttpEndpoint that)) {
            return false;
        }

        return Objects.equals(verb(), that.verb()) && Objects.equals(path(), that.path());
    }

    @Override
    public int hashCode() {
        return Objects.hash(verb(), path());
    }
}
