package io.dropwizard.hibernate.multitenant.listeners;

import io.dropwizard.hibernate.UnitOfWork;
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

    private final String tenantHeader;

    private final Map<String, Map<String, SessionFactory>> tenantDatabasesMap;

    private ConcurrentMap<String, ConcurrentMap<ResourceMethod, Optional<UnitOfWork>>> tenantMethodMap = new ConcurrentHashMap<>();

    public MultiTenantApplicationListener(String tenantHeader,
                                          Map<String, Map<String, SessionFactory>> tenantDatabasesMap) {
        this.tenantHeader = tenantHeader;
        this.tenantDatabasesMap= tenantDatabasesMap;
    }

    @Override
    public void onEvent(ApplicationEvent event) { }

    @Override
    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return new MultiTenantRequestListener(tenantHeader, tenantDatabasesMap, tenantMethodMap);
    }
}
