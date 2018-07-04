package io.dropwizard.hibernate.multitenant.listeners;

import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.multitenant.contexts.TenantRequestContext;
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

    private final String tenantHeader;

    private final Map<String, Map<String, SessionFactory>> tenantSessionFactories;

    private final ConcurrentMap<String, ConcurrentMap<ResourceMethod, Optional<UnitOfWork>>> tenantMethodMap;

    private final MultiTenantUnitOfWorkAspect unitOfWorkAspect;

    public MultiTenantRequestListener(String tenantHeader,
                                      Map<String, Map<String, SessionFactory>> tenantSessionFactories,
                                      ConcurrentMap<String, ConcurrentMap<ResourceMethod, Optional<UnitOfWork>>> tenantMethodMap) {
        this.tenantHeader = tenantHeader;
        this.tenantSessionFactories = tenantSessionFactories;
        this.tenantMethodMap = tenantMethodMap;
        this.unitOfWorkAspect = new MultiTenantUnitOfWorkAspect(tenantSessionFactories);
    }

    @Override
    public void onEvent(RequestEvent event) {
        String tenantId = event.getContainerRequest().getHeaderString(tenantHeader);
        TenantRequestContext.TENANT.set(tenantId);
        final RequestEvent.Type eventType = event.getType();
        if (eventType == RequestEvent.Type.RESOURCE_METHOD_START) {
            try {
                Optional<UnitOfWork> optionalUnitOfWork = this.tenantMethodMap.get(tenantId).computeIfAbsent(
                        event.getUriInfo().getMatchedResourceMethod(),
                        MultiTenantRequestListener::registerUnitOfWorkAnnotations);
                unitOfWorkAspect.beforeStart(tenantId, optionalUnitOfWork.orElse(null));
            }
            catch (Exception e) {
                throw new MappableException(e);
            }
        }
        else if (eventType == RequestEvent.Type.RESP_FILTERS_START) {
            try {
                unitOfWorkAspect.afterEnd();
            } catch (Exception e) {
                throw new MappableException(e);
            }
        }
        else if (eventType == RequestEvent.Type.ON_EXCEPTION) {
            unitOfWorkAspect.onError();
        }
        else if (eventType == RequestEvent.Type.FINISHED) {
            unitOfWorkAspect.onFinish();
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
