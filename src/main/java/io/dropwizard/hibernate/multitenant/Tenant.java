package io.dropwizard.hibernate.multitenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public final class Tenant {

    @Getter
    @JsonProperty
    private String id;

    @Getter
    @JsonProperty
    private Map<String, String> properties;

    @Getter
    @JsonProperty
    private Map<String, DataSourceFactory> databases;
}
