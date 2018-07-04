package io.dropwizard.hibernate.multitenant;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.ScanningHibernateBundle;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.Setter;


public class MultiTenantHibernateBundle<T extends Configuration> implements ConfiguredBundle<T> {
    public static final String DEFAULT_NAME = HibernateBundle.DEFAULT_NAME;

    @Getter
    @Setter
    private boolean lazyLoadingEnabled = true;

    private final ImmutableList<Class<?>> entities;
    private final SessionFactoryFactory sessionFactoryFactory;

    public MultiTenantHibernateBundle(String[] pckgs) {
        this(ScanningHibernateBundle.findEntityClassesFromDirectory(pckgs),
                new SessionFactoryFactory());
    }

    public MultiTenantHibernateBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entity).add(entities).build(),
                new SessionFactoryFactory());
    }

    public MultiTenantHibernateBundle(ImmutableList<Class<?>> entities,
                                      SessionFactoryFactory sessionFactoryFactory) {
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

    /**
     * Override to configure the name of the bundle
     * (It's used for the bundle health check and database pool metrics)
     */
    protected String name() {
        return DEFAULT_NAME;
    }

    public void run(T configuration, Environment environment) throws Exception {

    }

    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(this.createHibernate5Module());
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) { }
}
