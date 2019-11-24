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

import java.time.Instant;
import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pete
 */
public class Search {

    public static void main(String[] args) {
        KafkaTopicSearch consumer = new KafkaTopicSearch();
        consumer.setTopicToConsume("cerberus-search"); //Topic to consume from
        consumer.setStartFromTimestamp(Instant.now().minus(300, MINUTES).toEpochMilli());//Instant.now().minus(10, MINUTES).toEpochMilli()
        consumer.setTimeoutTime(Instant.now().plus(1, MINUTES).toEpochMilli());
        List<Condition> conditions = new ArrayList<>();
        Condition condition = new Condition();
        condition.setPath("$.customer.name");
        condition.setValue("Bob");
        conditions.add(condition);
        consumer.setConditions(conditions);
        boolean found = consumer.search();
        System.out.println(" Found " + found);
    }
}
