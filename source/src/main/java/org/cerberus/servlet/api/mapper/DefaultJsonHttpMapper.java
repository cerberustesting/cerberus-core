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
package org.cerberus.servlet.api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cerberus.servlet.api.HttpMapper;
import org.cerberus.util.json.ObjectMapperUtil;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;

public class DefaultJsonHttpMapper implements HttpMapper {

    private static final String CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;
    private static final String CHARACTER_ENCODING = Charset.forName("UTF-8").toString();

    private final ObjectMapper objectMapper;

    public DefaultJsonHttpMapper() {
        objectMapper = ObjectMapperUtil.newDefaultInstance();
    }

    @Override
    public String serialize(final Object data) throws HttpSerializationException {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (final JsonProcessingException e) {
            throw new HttpSerializationException(e);
        }
    }

    @Override
    public <T> T deserialize(final String payload, final Class<T> type) throws HttpDeserializationException {
        try {
            return objectMapper.readValue(payload, type);
        } catch (final IOException e) {
            throw new HttpDeserializationException(e);
        }
    }

    @Override
    public String getRequestContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public String getRequestCharacterEncoding() {
        return CHARACTER_ENCODING;
    }

    @Override
    public String getResponseContentType() {
        return CONTENT_TYPE;
    }

    @Override
    public String getResponseCharacterEncoding() {
        return CHARACTER_ENCODING;
    }

}
