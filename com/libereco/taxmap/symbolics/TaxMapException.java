package com.libereco.taxmap.symbolics;

import com.libereco.taxmap.symbolics.config.ConfigurableException;

/**
 * Base TaxMap exception
 *
 */
public class TaxMapException extends ConfigurableException {

    /**
     * Constructor.
     * Creates a new Exception by using super(msg) method.
     * @param errorMessage the description of the error
     *
     */
    public TaxMapException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructor.
     * Creates a new Exception by using super(msg, cause) method.
     * @param errorMessage the description of the error
     * @param cause            the cause
     *
     */
    public TaxMapException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
