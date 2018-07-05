package io.dropwizard.hibernate.multitenant.contexts;

import io.dropwizard.hibernate.multitenant.Tenant;

public class TenantRequestContext {
    public static final ThreadLocal<Tenant> TENANT = new ThreadLocal<>();
}
