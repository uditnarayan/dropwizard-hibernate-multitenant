package io.dropwizard.hibernate.multitenant;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.hibernate.UnitOfWork;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.hibernate.SessionFactory;

import java.lang.reflect.InvocationTargetException;

public class UnitOfWorkAwareMultiTenantProxyFactory {

    private final ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories;

    public UnitOfWorkAwareMultiTenantProxyFactory(
            ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories) {
        this.tenantSessionFactories = tenantSessionFactories;
    }


    /**
     * Creates a new <b>@UnitOfWork</b> aware proxy of a class with the default constructor.
     *
     * @param clazz the specified class definition
     * @param <T>   the type of the class
     * @return a new proxy
     */
    public <T> T create(Class<T> clazz) {
        return create(clazz, new Class<?>[]{}, new Object[]{});
    }

    /**
     * Creates a new <b>@UnitOfWork</b> aware proxy of a class with an one-parameter constructor.
     *
     * @param clazz                the specified class definition
     * @param constructorParamType the type of the constructor parameter
     * @param constructorArguments the argument passed to the constructor
     * @param <T>                  the type of the class
     * @return a new proxy
     */
    public <T> T create(Class<T> clazz, Class<?> constructorParamType, Object constructorArguments) {
        return create(clazz, new Class<?>[]{constructorParamType}, new Object[]{constructorArguments});
    }

    /**
     * Creates a new <b>@UnitOfWork</b> aware proxy of a class with a complex constructor.
     *
     * @param clazz                 the specified class definition
     * @param constructorParamTypes the types of the constructor parameters
     * @param constructorArguments  the arguments passed to the constructor
     * @param <T>                   the type of the class
     * @return a new proxy
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clazz, Class<?>[] constructorParamTypes, Object[] constructorArguments) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);

        try {
            final Proxy proxy = (Proxy) (constructorParamTypes.length == 0 ?
                    factory.createClass().getConstructor().newInstance() :
                    factory.create(constructorParamTypes, constructorArguments));
            proxy.setHandler((self, overridden, proceed, args) -> {
                final UnitOfWork unitOfWork = overridden.getAnnotation(UnitOfWork.class);
                final MultiTenantUnitOfWorkAspect unitOfWorkAspect = newAspect();
                try {
                    unitOfWorkAspect.beforeStart(unitOfWork);
                    Object result = proceed.invoke(self, args);
                    unitOfWorkAspect.afterEnd();
                    return result;
                } catch (InvocationTargetException e) {
                    unitOfWorkAspect.onError();
                    throw e.getCause();
                } catch (Exception e) {
                    unitOfWorkAspect.onError();
                    throw e;
                } finally {
                    unitOfWorkAspect.onFinish();
                }
            });
            return (T) proxy;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            throw new IllegalStateException("Unable to create a proxy for the class '" + clazz + "'", e);
        }
    }

    /**
     * @return a new aspect
     */
    public MultiTenantUnitOfWorkAspect newAspect() {
        return newAspect(this.tenantSessionFactories);
    }

    /**
     * @return a new aspect
     * @param tenantSessionFactories
     */
    public MultiTenantUnitOfWorkAspect newAspect(
            ImmutableMap<String, ImmutableMap<String, SessionFactory>> tenantSessionFactories) {
        return new MultiTenantUnitOfWorkAspect(tenantSessionFactories);
    }
}
