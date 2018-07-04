package io.dropwizard.hibernate.multitenant.contexts;

public class TenantRequestContext {
    public static final ThreadLocal<String> TENANT = new ThreadLocal<>();
}
