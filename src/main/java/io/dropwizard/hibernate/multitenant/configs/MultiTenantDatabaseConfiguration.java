package io.dropwizard.hibernate.multitenant.configs;

import io.dropwizard.Configuration;
import io.dropwizard.hibernate.multitenant.Tenant;

import java.util.List;

public interface MultiTenantDatabaseConfiguration<T extends Configuration> {
    List<Tenant> getTenants(T configuration);

    String getTenantHeaderPropertyName(T configuration);
}
