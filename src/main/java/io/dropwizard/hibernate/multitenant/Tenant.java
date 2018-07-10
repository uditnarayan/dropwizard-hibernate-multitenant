package io.dropwizard.hibernate.multitenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class Tenant {

    @JsonProperty
    private String id;

    @JsonProperty
    private Map<String, String> properties;

    @JsonProperty
    private Map<String, DataSourceFactory> databases;

    public Tenant() {
        this.properties = new HashMap<>();
        this.databases = new HashMap<>();
    }

    public Tenant(String id, Map<String, String> properties, Map<String, DataSourceFactory> databases) {
        this.id = id;
        this.properties = properties;
        this.databases = databases;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Map<String, DataSourceFactory> getDatabases() {
        return databases;
    }

    public boolean getBooleanProperty(String property) {
        return Boolean.parseBoolean(this.properties.get(property));
    }

    public int getIntegerProperty(String property) {
        return Integer.parseInt(this.properties.get(property));
    }

    public float getFloatProperty(String property) {
        return Float.parseFloat(this.properties.get(property));
    }

    public char getCharacterProperty(String property) {
        return this.properties.get(property).charAt(0);
    }

    public double getDoubleProperty(String property) {
        return Double.parseDouble(this.properties.get(property));
    }

    public String getStringProperty(String property) {
        return this.properties.get(property);
    }

    public Date getDateProperty(String property, SimpleDateFormat format) throws ParseException {
        String dateString = this.properties.getOrDefault(property, null);
        return format.parse(dateString);
    }
}
