package io.dropwizard.hibernate.multitenant;

import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public final class Tenant {

    @Getter
    private final String id;

    @Getter
    private final Map<String, DataSourceFactory> databases;

    public Tenant(String id, Map<String, DataSourceFactory> databases) {
        this.id = id;
        this.databases = new HashMap<>(databases);
    }
}
