package io.dropwizard.hibernate.multitenant;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.ScanningHibernateBundle;
import io.dropwizard.hibernate.multitenant.configs.MultiTenantDatabaseConfiguration;
import io.dropwizard.hibernate.multitenant.listeners.MultiTenantApplicationListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.SessionFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public abstract class MultiTenantHibernateBundle<T extends Configuration>
        implements ConfiguredBundle<T>, MultiTenantDatabaseConfiguration<T> {

    public static final String DEFAULT_NAME = HibernateBundle.DEFAULT_NAME;

    @Nullable
    private ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories;

    @Getter
    @Setter
    private boolean lazyLoadingEnabled = true;

    private final ImmutableList<Class<?>> entities;
    private final MultiTenantSessionFactoryFactory sessionFactoryFactory;

    public MultiTenantHibernateBundle(String[] pckgs) {
        this(ScanningHibernateBundle.findEntityClassesFromDirectory(pckgs),
                new MultiTenantSessionFactoryFactory());
    }

    public MultiTenantHibernateBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entity).add(entities).build(),
                new MultiTenantSessionFactoryFactory());
    }

    public MultiTenantHibernateBundle(ImmutableList<Class<?>> entities,
                                      MultiTenantSessionFactoryFactory sessionFactoryFactory) {
        this.entities = entities;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    /**
     * Override to configure the {@link Hibernate5Module}.
     */
    protected Hibernate5Module createHibernate5Module() {
        Hibernate5Module module = new Hibernate5Module();
        if (this.lazyLoadingEnabled) {
            module.enable(Hibernate5Module.Feature.FORCE_LAZY_LOADING);
        }
        return module;
    }

    public void run(T configuration, Environment environment) throws Exception {
        List<Tenant> tenants = this.getTenants(configuration);

        ImmutableMap.Builder<String, ImmutableMap<String, SessionFactory>> tenantSessionFactoriesBuilder;
        tenantSessionFactoriesBuilder = ImmutableMap.builder();

        for(Tenant tenant : tenants) {
            final ImmutableMap.Builder<String, SessionFactory> sessionFactoriesBuilder;
            sessionFactoriesBuilder = ImmutableMap.builder();

            for(Map.Entry<String, DataSourceFactory> entry: tenant.getDatabases().entrySet()) {
                PooledDataSourceFactory dataSourceFactory = entry.getValue();
                String name = String.format("%s-%s", tenant.getId(), entry.getKey());
                SessionFactory sessionFactory = this.sessionFactoryFactory.build(
                        this, environment, dataSourceFactory, entities, name);
                sessionFactoriesBuilder.put(name, sessionFactory);
            }

            ImmutableMap<String, SessionFactory> sessionFactories = sessionFactoriesBuilder.build();
            tenantSessionFactoriesBuilder.put(tenant.getId(), sessionFactories);
        }

        this.tenantSessionFactories = tenantSessionFactoriesBuilder.build();
        this.registerMultiTenantApplicationListenerIfAbsent(configuration, environment);
    }

    private MultiTenantApplicationListener registerMultiTenantApplicationListenerIfAbsent(T configuration,
                                                                                          Environment environment) {
        for (Object singleton : environment.jersey().getResourceConfig().getSingletons()) {
            if (singleton instanceof MultiTenantApplicationListener) {
                return (MultiTenantApplicationListener) singleton;
            }
        }
        final MultiTenantApplicationListener listener = new MultiTenantApplicationListener(
                this.getTenantResolver(configuration), this.tenantSessionFactories);
        environment.jersey().register(listener);
        return listener;
    }

    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(this.createHibernate5Module());
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) { }

    public ImmutableMap<String, ImmutableMap<String, SessionFactory>> getTenantSessionFactories() {
        return requireNonNull(this.tenantSessionFactories);
    }

    @Override
    public T getApplicationConfiguration(T configuration) {
        return configuration;
    }
}
