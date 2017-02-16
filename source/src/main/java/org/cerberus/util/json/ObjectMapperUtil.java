package org.cerberus.util.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
     *     <li>Ignore empty bean during serialization</li>
     *     <li>Ignore unknown properties during deserialization</li>
     *     <li>Accept single value to be deserialized as array if necessary</li>
     * </ul>
     *
     * @return a new {@link ObjectMapper} instance with additional configuration
     */
    public static ObjectMapper newDefaultInstance() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return objectMapper;
    }

}
