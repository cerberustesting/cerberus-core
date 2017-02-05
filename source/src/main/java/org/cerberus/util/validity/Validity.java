package org.cerberus.util.validity;

/**
 * Help to know if the implementing object if valid or not.
 *
 * @author abourdon
 */
public interface Validity {

    /**
     * Check if this object is valid or not.
     *
     * @return <code>true</code> if this object is valid, <code>false</code> otherwise
     */
    boolean isValid();

}
