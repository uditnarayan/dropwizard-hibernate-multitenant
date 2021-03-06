package io.dropwizard.hibernate.multitenant;

import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.SessionFactoryManager;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

@Slf4j
public class MultiTenantSessionFactoryFactory {
    private static final String DEFAULT_NAME = MultiTenantHibernateBundle.DEFAULT_NAME;

    public SessionFactory build(MultiTenantHibernateBundle<?> bundle,
                                Environment environment,
                                PooledDataSourceFactory dbConfig,
                                List<Class<?>> entities) {
        return build(bundle, environment, dbConfig, entities, DEFAULT_NAME);
    }

    public SessionFactory build(MultiTenantHibernateBundle<?> bundle,
                                Environment environment,
                                PooledDataSourceFactory dbConfig,
                                List<Class<?>> entities,
                                String name) {
        final ManagedDataSource dataSource = dbConfig.build(environment.metrics(), name);
        return build(bundle, environment, dbConfig, dataSource, entities);
    }

    public SessionFactory build(MultiTenantHibernateBundle<?> bundle,
                                Environment environment,
                                PooledDataSourceFactory dbConfig,
                                ManagedDataSource dataSource,
                                List<Class<?>> entities) {
        final ConnectionProvider provider = buildConnectionProvider(dataSource,
                dbConfig.getProperties());
        final SessionFactory factory = buildSessionFactory(bundle,
                dbConfig,
                provider,
                dbConfig.getProperties(),
                entities);
        final SessionFactoryManager managedFactory = new SessionFactoryManager(factory, dataSource);
        environment.lifecycle().manage(managedFactory);
        return factory;
    }

    private ConnectionProvider buildConnectionProvider(DataSource dataSource,
                                                       Map<String, String> properties) {
        final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        connectionProvider.configure(properties);
        return connectionProvider;
    }

    private SessionFactory buildSessionFactory(MultiTenantHibernateBundle<?> bundle,
                                               PooledDataSourceFactory dbConfig,
                                               ConnectionProvider connectionProvider,
                                               Map<String, String> properties,
                                               List<Class<?>> entities) {
        final Configuration configuration = new Configuration();
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
        configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS, Boolean.toString(dbConfig.isAutoCommentsEnabled()));
        configuration.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
        configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
        configuration.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
        configuration.setProperty(AvailableSettings.ORDER_UPDATES, "true");
        configuration.setProperty(AvailableSettings.ORDER_INSERTS, "true");
        configuration.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        configuration.setProperty("jadira.usertype.autoRegisterUserTypes", "true");
        for (Map.Entry<String, String> property : properties.entrySet()) {
            configuration.setProperty(property.getKey(), property.getValue());
        }

        addAnnotatedClasses(configuration, entities);
        bundle.configure(configuration);

        final ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .addService(ConnectionProvider.class, connectionProvider)
                .applySettings(configuration.getProperties())
                .build();

        configure(configuration, registry);

        return configuration.buildSessionFactory(registry);
    }

    protected void configure(Configuration configuration, ServiceRegistry registry) { }

    private void addAnnotatedClasses(Configuration configuration,
                                     Iterable<Class<?>> entities) {
        final SortedSet<String> entityClasses = new TreeSet<>();
        for (Class<?> klass : entities) {
            configuration.addAnnotatedClass(klass);
            entityClasses.add(klass.getCanonicalName());
        }
        log.info("Entity classes: {}", entityClasses);
    }
}
