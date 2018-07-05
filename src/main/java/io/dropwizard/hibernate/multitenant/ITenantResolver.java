package io.dropwizard.hibernate.multitenant;

import io.dropwizard.hibernate.multitenant.exceptions.MissingTenantException;
import org.glassfish.jersey.server.ContainerRequest;

public interface ITenantResolver {
    Tenant resolve(ContainerRequest containerRequest) throws MissingTenantException;
}
