package io.dropwizard.hibernate.multitenant;

import org.glassfish.jersey.server.ContainerRequest;

import java.util.Optional;

public interface ITenantResolver {
    Optional<Tenant> resolve(ContainerRequest containerRequest);
}
