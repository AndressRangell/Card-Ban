package com.cobranzas.cajas.model;

import android.support.annotation.Keep;

@Keep
public class ErrorJSON {

    private int statusCode;
    private String error;
    private String message;

    public ErrorJSON() {
        // Do nothing because of X and Y.
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
