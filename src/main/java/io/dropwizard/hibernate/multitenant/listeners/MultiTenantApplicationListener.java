package io.dropwizard.hibernate.multitenant.listeners;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.multitenant.ITenantResolver;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.hibernate.SessionFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MultiTenantApplicationListener implements ApplicationEventListener {

    private final ITenantResolver tenantResolver;

    private final ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories;

    private ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> resourceMethodMap = new ConcurrentHashMap<>();

    public MultiTenantApplicationListener(
            ITenantResolver tenantResolver,
            ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories) {
        this.tenantResolver = tenantResolver;
        this.tenantSessionFactories= tenantSessionFactories;
    }

    @Override
    public void onEvent(ApplicationEvent event) { }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new MultiTenantRequestListener(tenantResolver, tenantSessionFactories, resourceMethodMap);
    }
}
