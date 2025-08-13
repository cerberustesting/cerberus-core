/**
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
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
package org.cerberus.core.service.json.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.cerberus.core.crud.entity.TestCaseStepActionControl;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.enums.MessageEventEnum;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;

@Service
public class JsonSchemaValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get differences between json object and a json schema
     * @param jsonToValidate
     * @param jsonSchema
     * @return a List of differences in the format Set<ValidationMessage>
     * @throws Exception
     */
    public Set<ValidationMessage> getDifferences(String jsonToValidate, String jsonSchema) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode schemaNode = objectMapper.readTree(jsonSchema);
        JsonNode validateNode = mapper.readTree(jsonToValidate);

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(schemaNode);
        return schema.validate(validateNode);

    }
}