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

/**
 *
 * @author Pete
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.logging.log4j.Logger;

/**
 * Kafka Consumer that manages it's own partition assignments and doesn't commit
 * offsets as it is not processing records just searching
 *
 * Used as a simple topic search facility
 *
 * @author Pete
 */
public class KafkaTopicSearch {

    private KafkaConsumer consumer;
    private boolean consume = true;
    private final Properties consumerProps = new Properties();
    private String topicToConsume = "cerberus";
    private long startFromTimestamp = 0l;
    private long timeoutTime = 30000; //default to 30 seconds
    private List<Condition> conditions = new ArrayList<>();
    private boolean found = false;
    protected final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(getClass());

    public KafkaTopicSearch() {
        //Set up the consumer
        consumerProps.put("bootstrap.servers", "public-kafka-poc-redoute-c3aa.aivencloud.com:27163");
        consumerProps.put("enable.auto.commit", "false"); //no commiting
        consumerProps.put("max.poll.records", 10); //For speed we can process multiple records at a time
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        
        consumerProps.put("security.protocol", "SSL");
        consumerProps.put("ssl.endpoint.identification.algorithm", "");
        consumerProps.put("ssl.truststore.location", "/home/ben/dev/cerberus-media/client.truststore.jks");
        consumerProps.put("ssl.truststore.password", "secret");
        consumerProps.put("ssl.keystore.type", "PKCS12");
        consumerProps.put("ssl.keystore.password", "secret");
        consumerProps.put("ssl.keystore.location", "/home/ben/dev/cerberus-media/client.keystore.p12");
        
        consumer = new KafkaConsumer<>(consumerProps);
        final Thread mainThread = Thread.currentThread();
        //Shutdown hook to allow clean finish in the case of interruption i.e. pod delete etc 
        //https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html#callout_kafka_consumers__reading_data_from_kafka_CO2-1
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                consumer.wakeup();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    // do nothing we are shutting down 
                }
            }
        });
    }

    protected boolean search() {
        //Get a list of the topics' partitions
        List<PartitionInfo> partitionList = (List<PartitionInfo>) consumer.partitionsFor(this.topicToConsume);
        List<TopicPartition> topicPartitionList = partitionList.stream().map(info -> new TopicPartition(this.topicToConsume, info.partition())).collect(Collectors.toList());
        //Assign all the partitions to this consumer
        consumer.assign(topicPartitionList);
        consumer.seekToEnd(topicPartitionList); //default to latest offset for all partitions
        if (this.startFromTimestamp > 0l) {
            //Format a Query to get the partitions/offset information to determine the offsets for records that are at the timestamp
            Map<TopicPartition, Long> query = new HashMap<>();
            for (TopicPartition partition : topicPartitionList) {
                query.put(partition, this.startFromTimestamp);
            }
            //Execute the qyery to reset the offset to the timestamp for each partition
            //If there is no corresponding offset for a timestamp in a partition it will return null and we'll leave the offset as set above at latest
            Map<TopicPartition, OffsetAndTimestamp> partitionOffsetForTimestamp = (Map<TopicPartition, OffsetAndTimestamp>) consumer.offsetsForTimes(query);
            for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : partitionOffsetForTimestamp.entrySet()) {
                if (entry.getValue() != null) {
                    //If we have an offset that corresponds to a timestamp then reset the offset for partition
                    consumer.seek(entry.getKey(), entry.getValue().offset());
                    LOGGER.info("Setting offset for partition " + entry.getKey().partition() + " to " + entry.getValue().offset());
                } else {
                    LOGGER.info("Leaving offset for partition " + entry.getKey().partition() + " to Latest");
                }
            }
        }
        try {
            while (consume) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
                if (Instant.now().toEpochMilli() > this.timeoutTime) {
                    LOGGER.info("Timed out searching for record");
                    consumer.wakeup(); //exit
                }
                //Now for each record in the batch of records we got from Kafka
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        boolean allCriteriaMet = true;
                        for (Condition criterion : conditions) {
                            //If one condition is false then don't bother testing any of the remaining conditions
                            //as were are AND'ing all the conditions
                            if (!criterion.test(record.value())) {
                                allCriteriaMet = false;
                                break;
                            }
                        }
                        if (allCriteriaMet) {
                            LOGGER.info("Found matching record " + record.topic() + " " + record.partition() + " " + record.offset());
                            found = true;
                            consume = false;  //exit the consume loop
                            consumer.wakeup(); //takes effect on the next poll loop so need to break.
                            break; //if we've found a match, stop looping through the current record batch
                        }

                    } catch (Exception ex) {
                        //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                        //but we don't want to trigger the catch block for Kafka consumption
                        LOGGER.error(ex);
                    }
                }
            }
        } catch (WakeupException e) {
            //Ignore
        } catch (Exception ex) {
            LOGGER.error(ex);
        } finally {
            close();
        }
        return found;
    }

    protected void close() {
        if (consumer != null) {
            consumer.close();
        }
    }

    public void setTopicToConsume(String topicToConsume) {
        this.topicToConsume = topicToConsume;
    }

    public void setStartFromTimestamp(long startFromTimestamp) {
        this.startFromTimestamp = startFromTimestamp;
    }

    public void setTimeoutTime(long timeoutTime) {
        this.timeoutTime = timeoutTime;
    }

    public void setConditions(List<Condition> selectionCriteria) {
        this.conditions = selectionCriteria;
    }
}
