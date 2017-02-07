package org.cerberus.servlet.api;

import org.cerberus.util.validity.Validity;

/**
 * Default implementation for empty {@link SinglePointHttpServlet}'s request
 *
 * @author abourdon
 */
public final class EmptyRequest implements Validity {

    @Override
    public boolean isValid() {
        return true;
    }

}
