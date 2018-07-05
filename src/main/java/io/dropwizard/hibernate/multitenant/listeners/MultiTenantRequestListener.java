package io.dropwizard.hibernate.multitenant.listeners;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.multitenant.ITenantResolver;
import io.dropwizard.hibernate.multitenant.Tenant;
import io.dropwizard.hibernate.multitenant.contexts.TenantRequestContext;
import io.dropwizard.hibernate.multitenant.exceptions.MissingTenantException;
import io.dropwizard.hibernate.multitenant.impl.MultiTenantUnitOfWorkAspect;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.hibernate.SessionFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public class MultiTenantRequestListener implements RequestEventListener {

    private final ITenantResolver tenantResolver;

    private final Map<String, Map<String, SessionFactory>> tenantSessionFactories;

    private final ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> resourceMethodMap;

    private final MultiTenantUnitOfWorkAspect unitOfWorkAspect;

    public MultiTenantRequestListener(ITenantResolver tenantResolver,
                                      Map<String, Map<String, SessionFactory>> tenantSessionFactories,
                                      ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> resourceMethodMap) {
        this.tenantResolver = tenantResolver;
        this.tenantSessionFactories = tenantSessionFactories;
        this.resourceMethodMap = resourceMethodMap;
        this.unitOfWorkAspect = new MultiTenantUnitOfWorkAspect(tenantSessionFactories);
    }

    @Override
    public void onEvent(RequestEvent event) {
        try {
            final RequestEvent.Type eventType = event.getType();
            if (eventType == RequestEvent.Type.RESOURCE_METHOD_START) {
                Tenant tenant = this.tenantResolver.resolve(event.getContainerRequest());
                TenantRequestContext.TENANT.set(tenant);
                try {
                    Optional<UnitOfWork> optionalUnitOfWork = this.resourceMethodMap.computeIfAbsent(
                            event.getUriInfo().getMatchedResourceMethod(),
                            MultiTenantRequestListener::registerUnitOfWorkAnnotations);
                    unitOfWorkAspect.beforeStart(tenant.getId(), optionalUnitOfWork.orElse(null));
                } catch (Exception e) {
                    throw new MappableException(e);
                }
            } else if (eventType == RequestEvent.Type.RESP_FILTERS_START) {
                try {
                    unitOfWorkAspect.afterEnd();
                } catch (Exception e) {
                    throw new MappableException(e);
                }
            } else if (eventType == RequestEvent.Type.ON_EXCEPTION) {
                unitOfWorkAspect.onError();
            } else if (eventType == RequestEvent.Type.FINISHED) {
                unitOfWorkAspect.onFinish();
            }
        }
        catch (MissingTenantException e) {
            throw new MappableException(e);
        }
    }

    private static Optional<UnitOfWork> registerUnitOfWorkAnnotations(ResourceMethod method) {
        UnitOfWork annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfWork.class);
        if (annotation == null) {
            annotation = method.getInvocable().getHandlingMethod().getAnnotation(UnitOfWork.class);
        }
        return Optional.ofNullable(annotation);
    }
}
