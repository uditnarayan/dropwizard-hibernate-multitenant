package io.dropwizard.hibernate.multitenant;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.multitenant.contexts.TenantRequestContext;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class MultiTenantUnitOfWorkAspect {

    private final ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories;

    @Nullable
    private UnitOfWork unitOfWork;

    @Nullable
    private Session session;

    @Nullable
    private SessionFactory sessionFactory;

    public MultiTenantUnitOfWorkAspect(ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories) {
        this.tenantSessionFactories = tenantSessionFactories;
    }

    private SessionFactory getTenantSessionFactory(String tenantId, UnitOfWork unitOfWork) {
        if (!this.tenantSessionFactories.containsKey(tenantId)) {
            String msg = String.format("Invalid tenant %s provided.", tenantId);
            throw new IllegalArgumentException(msg);
        }
        Map<String, SessionFactory> tenantSessionFactoryMap =
                this.tenantSessionFactories.getOrDefault(tenantId, null);
        Preconditions.checkNotNull(tenantSessionFactoryMap);

        String database = Strings.isNullOrEmpty(unitOfWork.value())
                ? String.format("%s-%s", tenantId, MultiTenantHibernateBundle.DEFAULT_NAME)
                : String.format("%s-%s", tenantId, unitOfWork.value());
        this.sessionFactory = tenantSessionFactoryMap.getOrDefault(database, null);
        Preconditions.checkNotNull(this.sessionFactory);
        return sessionFactory;
    }

    public void beforeStart(@Nullable UnitOfWork unitOfWork) {
        if (unitOfWork == null) {
            return;
        }
        Tenant tenant = TenantRequestContext.TENANT.get();
        requireNonNull(tenant);
        this.unitOfWork = unitOfWork;
        this.sessionFactory = this.getTenantSessionFactory(tenant.getId(), unitOfWork);
        this.session = this.sessionFactory.openSession();
        try {
            configureSession();
            ManagedSessionContext.bind(this.session);
            TenantRequestContext.HIBERNATE_SESSION_FACTORY.set(this.sessionFactory);
            beginTransaction(this.unitOfWork, this.session);
        } catch (Throwable th) {
            this.onFinish();
            throw th;
        }
    }

    public void afterEnd() {
        if (this.unitOfWork == null || this.session == null) {
            return;
        }

        try {
            this.commitTransaction(this.unitOfWork, this.session);
        } catch (Exception e) {
            this.rollbackTransaction(this.unitOfWork, this.session);
            throw e;
        }
        // We should not close the session to let the lazy loading work during serializing a response to the client.
        // If the response successfully serialized, then the session will be closed by the `onFinish` method
    }

    public void onError() {
        if (this.unitOfWork == null || this.session == null) {
            return;
        }

        try {
            this.rollbackTransaction(this.unitOfWork, this.session);
        } finally {
            onFinish();
        }
    }

    public void onFinish() {
        try {
            if (session != null) {
                session.close();
            }
        } finally {
            this.session = null;
            TenantRequestContext.HIBERNATE_SESSION_FACTORY.remove();
            ManagedSessionContext.unbind(sessionFactory);
        }
    }

    protected void configureSession() {
        if (this.unitOfWork == null || this.session == null) {
            throw new NullPointerException("unitOfWork or session is null. This is a bug!");
        }
        session.setDefaultReadOnly(this.unitOfWork.readOnly());
        session.setCacheMode(this.unitOfWork.cacheMode());
        session.setHibernateFlushMode(this.unitOfWork.flushMode());
    }

    private void beginTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        session.beginTransaction();
    }

    private void rollbackTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.getStatus().canRollback()) {
            txn.rollback();
        }
    }

    private void commitTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.getStatus().canRollback()) {
            txn.commit();
        }
    }

    protected Session getSession() {
        return requireNonNull(this.session);
    }

    protected SessionFactory getSessionFactory() {
        return requireNonNull(sessionFactory);
    }

}
