/**
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.api.errorhandler.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author mlombard
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class<?> clazz, String... searchParamsMap) {
        super(EntityNotFoundException.generateMessage(
                clazz.getSimpleName(),
                toMap(String.class, String.class, searchParamsMap)
        ));
    }

    private static String generateMessage(String entity, Map<String, String> searchParams) {
        return StringUtils.capitalize(entity)
                + " was not found for parameters "
                + searchParams;
    }

    private static <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType, Object... entries) {
        if (entries.length % 2 == 1) {
            throw new IllegalArgumentException("Invalid entries");
        }

        return IntStream
                .range(0, entries.length / 2)
                .map(i -> i * 2)
                .collect(
                        HashMap::new,
                        (m, i) -> m.put(
                                keyType.cast(entries[i]),
                                valueType.cast(entries[i + 1])
                        ),
                        Map::putAll
                );
    }

}
