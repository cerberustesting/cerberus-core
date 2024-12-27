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
package org.cerberus.core.crud.factory;

import java.sql.Timestamp;
import org.cerberus.core.crud.entity.AppService;

/**
 *
 * @author cte
 */
public interface IFactoryAppService {

    /**
     *
     * @param service
     * @param type
     * @param method
     * @param application
     * @param collection
     * @param bodyType
     * @param serviceRequest
     * @param kafkaTopic
     * @param kafkaKey
     * @param kafkaFilterPath
     * @param kafkaFilterValue
     * @param kafkaFilterHeaderPath
     * @param kafkaFilterHeaderValue
     * @param description
     * @param servicePath
     * @param isFollowRedir
     * @param attachementURL
     * @param operation
     * @param isAvroEnable
     * @param schemaRegistryUrl
     * @param isAvroEnableKey
     * @param avroSchemaKey
     * @param isAvroEnableValue
     * @param avroSchemaValue
     * @param parentContentService
     * @param usrCreated
     * @param dateCreated
     * @param usrModif
     * @param dateModif
     * @param fileName
     * @return
     */
    AppService create(String service, String type, String method, String application, String collection, String bodyType, String serviceRequest,
            String kafkaTopic, String kafkaKey, String kafkaFilterPath, String kafkaFilterValue, String kafkaFilterHeaderPath, String kafkaFilterHeaderValue,
            String description, String servicePath, boolean isFollowRedir, String attachementURL, String operation, boolean isAvroEnable, String schemaRegistryUrl, boolean isAvroEnableKey, String avroSchemaKey, boolean isAvroEnableValue, String avroSchemaValue, String parentContentService,
            String usrCreated, Timestamp dateCreated, String usrModif, Timestamp dateModif, String fileName);
}
