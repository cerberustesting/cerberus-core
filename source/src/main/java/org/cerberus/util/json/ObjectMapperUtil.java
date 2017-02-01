package org.cerberus.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utili
 *
 * @author abourdon
 */
public final class ObjectMapperUtil {

    /**
     * Create and return a new {@link ObjectMapper} instance with the following additional configurations:
     * <ul>
     *     <li>Ignore <code>null</code> values during serialization</li>
     * </ul>
     *
     * @return a new {@link ObjectMapper} instance with additional configuration
     */
    public static ObjectMapper newInstance() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

}
