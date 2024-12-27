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
package org.cerberus.core.service.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.TopicPartition;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.util.answer.AnswerItem;

/**
 *
 * @author vertigo17
 */
public interface IKafkaService {

    /**
     *
     * @param topic
     * @param bootstrapServers
     * @return
     */
    public String getKafkaConsumerKey(String topic, String bootstrapServers);

    /**
     *
     * @param topic
     * @param key
     * @param eventMessage
     * @param bootstrapServers
     * @param serviceHeader
     * @param serviceContent
     * @param token
     * @param isAvroEnable
     * @param schemaRegistryURL
     * @param isAvroEnableKey
     * @param avroSchemaKey
     * @param isAvroEnableValue
     * @param avroSchemaValue
     * @param timeoutMs
     * @return
     */
    public AnswerItem<AppService> produceEvent(String topic, String key, String eventMessage,
            String bootstrapServers, List<AppServiceHeader> serviceHeader, List<AppServiceContent> serviceContent, String token, 
            boolean isAvroEnable, String schemaRegistryURL, boolean isAvroEnableKey, String avroSchemaKey, boolean isAvroEnableValue, String avroSchemaValue, int timeoutMs);

    /**
     * Get the last offset of every partition.
     *
     * @param topic
     * @param bootstrapServers
     * @param kafkaProps
     * @param timeoutMs
     * @return a map that contain the last offset of every partition.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public AnswerItem<Map<TopicPartition, Long>> seekEvent(String topic, String bootstrapServers,
            List<AppServiceContent> kafkaProps, int timeoutMs) throws InterruptedException, ExecutionException;

    /**
     *
     * @param mapOffsetPosition
     * @param topic
     * @param bootstrapServers
     * @param filterPath
     * @param serviceContent
     * @param filterValue
     * @param serviceHeader
     * @param filterHeaderPath
     * @param targetNbEventsInt
     * @param filterHeaderValue
     * @param avroEnable
     * @param avroEnableKey
     * @param avroEnableValue
     * @param schemaRegistryURL
     * @param targetNbSecInt
     * @return
     */
    public AnswerItem<String> searchEvent(Map<TopicPartition, Long> mapOffsetPosition, String topic, String bootstrapServers,
            List<AppServiceHeader> serviceHeader, List<AppServiceContent> serviceContent, String filterPath, String filterValue, String filterHeaderPath, String filterHeaderValue,
            boolean avroEnable, String schemaRegistryURL, boolean avroEnableKey, boolean avroEnableValue, int targetNbEventsInt, int targetNbSecInt);

    /**
     * Get the latest Offset of all partitions. This is triggered at the
     * beginning of the execution only when at least a SEARCH KAFKA service is
     * called.
     *
     * @param mainExecutionTestCaseStepList
     * @param tCExecution
     * @return
     * @throws CerberusException
     */
    public HashMap<String, Map<TopicPartition, Long>> getAllConsumers(List<TestCaseStep> mainExecutionTestCaseStepList, TestCaseExecution tCExecution) throws CerberusException;

}
