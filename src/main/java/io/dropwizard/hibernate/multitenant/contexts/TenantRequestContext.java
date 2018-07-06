package io.dropwizard.hibernate.multitenant.contexts;

import io.dropwizard.hibernate.multitenant.Tenant;
import org.hibernate.SessionFactory;

public class TenantRequestContext {
    public static final ThreadLocal<Tenant> TENANT = new ThreadLocal<>();
    public static final ThreadLocal<SessionFactory> HIBERNATE_SESSION_FACTORY = new ThreadLocal<>();
}
