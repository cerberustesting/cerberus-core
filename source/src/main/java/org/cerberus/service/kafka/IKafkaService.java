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
package org.cerberus.service.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.TopicPartition;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.answer.AnswerItem;

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
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public AnswerItem<AppService> produceEvent(String topic, String key, String eventMessage,
            String bootstrapServers, List<AppServiceHeader> serviceHeader) throws InterruptedException, ExecutionException;

    /**
     *
     * @param topic
     * @param bootstrapServers
     * @param serviceHeader
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public AnswerItem<Map<TopicPartition, Long>> seekEvent(String topic, String bootstrapServers,
            List<AppServiceHeader> serviceHeader) throws InterruptedException, ExecutionException;

    /**
     *
     * @param mapOffsetPosition
     * @param topic
     * @param bootstrapServers
     * @param filterPath
     * @param filterValue
     * @param serviceHeader
     * @param targetNbEventsInt
     * @param targetNbSecInt
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public AnswerItem<String> searchEvent(Map<TopicPartition, Long> mapOffsetPosition, String topic, String bootstrapServers,
            List<AppServiceHeader> serviceHeader, String filterPath, String filterValue, int targetNbEventsInt, int targetNbSecInt) throws InterruptedException, ExecutionException;

    /**
     *
     * @param mainExecutionTestCaseStepList
     * @param tCExecution
     * @return
     * @throws CerberusException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public HashMap<String, Map<TopicPartition, Long>> getAllConsumers(List<TestCaseStep> mainExecutionTestCaseStepList, TestCaseExecution tCExecution) throws CerberusException, InterruptedException, ExecutionException;

}
