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

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.util.answer.AnswerItem;

/**
 *
 * @author bcivel
 */
public interface IKafkaService {

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
     * @param filterPath
     * @param filterValue
     * @param bootstrapServers
     * @param serviceHeader
     * @param targetNbEventsInt
     * @param targetNbSecInt
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public AnswerItem<AppService> searchEvent(String topic, String filterPath, String filterValue,
            String bootstrapServers,
            List<AppServiceHeader> serviceHeader, int targetNbEventsInt, int targetNbSecInt) throws InterruptedException, ExecutionException;

}
