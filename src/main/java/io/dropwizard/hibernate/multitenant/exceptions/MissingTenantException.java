package io.dropwizard.hibernate.multitenant.exceptions;

public class MissingTenantException extends Exception {

    public MissingTenantException() {
        super();
    }

    public MissingTenantException(String message) {
        super(message);
    }

    public MissingTenantException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingTenantException(Throwable cause) {
        super(cause);
    }

    protected MissingTenantException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
