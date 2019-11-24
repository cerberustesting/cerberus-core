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
package org.cerberus.service.kafka.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.logging.log4j.Logger;
import org.cerberus.crud.entity.AppService;
import org.cerberus.crud.entity.AppServiceHeader;
import org.cerberus.crud.factory.IFactoryAppService;
import org.cerberus.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.crud.service.IAppServiceService;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.engine.entity.MessageEvent;
import org.cerberus.engine.execution.IRecorderService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.service.kafka.Condition;
import org.cerberus.service.kafka.IKafkaService;
import org.cerberus.service.proxy.IProxyService;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Producer is a one shot producer in that it writes a single record then closes
 * down
 *
 * @author Pete
 */
@Service
public class KafkaService implements IKafkaService {

    @Autowired
    IRecorderService recorderService;
    @Autowired
    IFactoryAppServiceHeader factoryAppServiceHeader;
    @Autowired
    IParameterService parameterService;
    @Autowired
    IFactoryAppService factoryAppService;
    @Autowired
    IAppServiceService AppServiceService;
    @Autowired
    IProxyService proxyService;

    protected final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(getClass());

    @Override
    public AnswerItem<AppService> produceEvent(String topic, String key, String eventMessage,
            String bootstrapServers,
            List<AppServiceHeader> serviceHeader) throws InterruptedException, ExecutionException {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_PRODUCEKAFKA);;
        AnswerItem<AppService> result = new AnswerItem<>();
        AppService serviceREST = factoryAppService.create("", AppService.TYPE_KAFKA, AppService.METHOD_KAFKAPRODUCE, "", "", "", "", "", "", "", "", "", "", "",
                "", null, "", null, null);

        Properties props = new Properties();
        serviceHeader.add(factoryAppServiceHeader.create(null, "bootstrap.servers", bootstrapServers, "Y", 0, "", "", null, "", null));
        serviceHeader.add(factoryAppServiceHeader.create(null, "enable.idempotence", "true", "Y", 0, "", "", null, "", null));
        serviceHeader.add(factoryAppServiceHeader.create(null, "key.serializer", "org.apache.kafka.common.serialization.StringSerializer", "Y", 0, "", "", null, "", null));
        serviceHeader.add(factoryAppServiceHeader.create(null, "value.serializer", "org.apache.kafka.common.serialization.StringSerializer", "Y", 0, "", "", null, "", null));

        for (AppServiceHeader object : serviceHeader) {
            if (StringUtil.parseBoolean(object.getActive())) {
                props.put(object.getKey(), object.getValue());
            }
        }

        serviceREST.setServicePath(bootstrapServers);
        serviceREST.setKafkaTopic(topic);
        serviceREST.setKafkaKey(key);
        serviceREST.setServiceRequest(eventMessage);
        serviceREST.setHeaderList(serviceHeader);

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        int partition = -1;
        long offset = -1;
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, eventMessage);
            LOG.debug("Producing Kafka message - topic : " + topic + " key : " + key + " message : " + eventMessage);
            RecordMetadata metadata = producer.send(record).get(); //Wait for a responses
            partition = metadata.partition();
            offset = metadata.offset();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_PRODUCEKAFKA);
        } catch (Exception ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_PRODUCEKAFKA);
            message.setDescription(message.getDescription().replace("%EX%", ex.toString()));
            LOG.debug(ex, ex);
        } finally {
            producer.flush();
            producer.close();
            LOG.info("Closed producer");
        }

        serviceREST.setKafkaResponseOffset(offset);
        serviceREST.setKafkaResponsePartition(partition);

        serviceREST.setResponseHTTPBodyContentType(AppServiceService.guessContentType(serviceREST, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));

        result.setItem(serviceREST);
        message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", AppService.METHOD_KAFKAPRODUCE));
        message.setDescription(message.getDescription().replace("%TOPIC%", topic));
        message.setDescription(message.getDescription().replace("%PART%", String.valueOf(partition)));
        message.setDescription(message.getDescription().replace("%OFFSET%", String.valueOf(offset)));
        result.setResultMessage(message);
        return result;
    }

    @Override
    public AnswerItem<AppService> seekEvent(String topic, String key, String eventMessage,
            String bootstrapServers,
            List<AppServiceHeader> serviceHeader, int targetNbEventsInt, int targetNbSecInt) throws InterruptedException, ExecutionException {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKKAFKA);
        AnswerItem<AppService> result = new AnswerItem<>();
        AppService serviceREST = factoryAppService.create("", AppService.TYPE_KAFKA, AppService.METHOD_KAFKASEEK, "", "", "", "", "", "", "", "", "", "", "", "", null, "", null, null);

        if (targetNbEventsInt <= 0) {
            // We get at least 1 Event.
            targetNbEventsInt = 1;
        }
        if (targetNbSecInt <= 0) {
            // We wait at least 1 second.
            targetNbSecInt = 1;
        }

        KafkaConsumer consumer = null;

        serviceREST.setKafkaResponsePartition(-1);
        serviceREST.setKafkaResponseOffset(-1);
        JSONArray resultJSON = new JSONArray();

        try {

            Properties props = new Properties();
            serviceHeader.add(factoryAppServiceHeader.create(null, "bootstrap.servers", bootstrapServers, "Y", 0, "", "", null, "", null));
            serviceHeader.add(factoryAppServiceHeader.create(null, "enable.auto.commit", "false", "Y", 0, "", "", null, "", null));
            serviceHeader.add(factoryAppServiceHeader.create(null, "max.poll.records", "10", "Y", 0, "", "", null, "", null));
            serviceHeader.add(factoryAppServiceHeader.create(null, "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer", "Y", 0, "", "", null, "", null));
            serviceHeader.add(factoryAppServiceHeader.create(null, "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer", "Y", 0, "", "", null, "", null));

            for (AppServiceHeader object : serviceHeader) {
                if (StringUtil.parseBoolean(object.getActive())) {
                    props.put(object.getKey(), object.getValue());
                }
            }

            serviceREST.setServicePath(bootstrapServers);
            serviceREST.setKafkaTopic(topic);
            serviceREST.setKafkaKey(key);
            serviceREST.setServiceRequest(eventMessage);
            serviceREST.setHeaderList(serviceHeader);
            serviceREST.setKafkaWaitNbEvent(targetNbEventsInt);
            serviceREST.setKafkaWaitSecond(targetNbSecInt);

            consumer = new KafkaConsumer<>(props);
            final Thread mainThread = Thread.currentThread();
            //Shutdown hook to allow clean finish in the case of interruption i.e. pod delete etc 
            //https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html#callout_kafka_consumers__reading_data_from_kafka_CO2-1
//            Runtime.getRuntime().addShutdownHook(new Thread() {
//                public void run() {
//                    consumer.wakeup();
//                    try {
//                        mainThread.join();
//                    } catch (InterruptedException e) {
//                        // do nothing we are shutting down 
//                    }
//                }
//            });

            long startFromTimestamp = 0l;
            //Get a list of the topics' partitions
            List<PartitionInfo> partitionList = consumer.partitionsFor(topic);
            List<TopicPartition> topicPartitionList = partitionList.stream().map(info -> new TopicPartition(topic, info.partition())).collect(Collectors.toList());
            //Assign all the partitions to this consumer
            consumer.assign(topicPartitionList);
            consumer.seekToEnd(topicPartitionList); //default to latest offset for all partitions

            Map<TopicPartition, OffsetAndTimestamp> partitionOffset = consumer.endOffsets(topicPartitionList);
            for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : partitionOffset.entrySet()) {
                TopicPartition keyPart = entry.getKey();
//                OffsetAndTimestamp valOffset = entry.getValue();
                LOG.debug("Partition : " + keyPart.partition() + " - Offset : " + entry.getValue());
            }

//        if (startFromTimestamp > 0l) {
//            //Format a Query to get the partitions/offset information to determine the offsets for records that are at the timestamp
//            Map<TopicPartition, Long> query = new HashMap<>();
//            for (TopicPartition partition : topicPartitionList) {
//                query.put(partition, startFromTimestamp);
//            }
//            //Execute the query to reset the offset to the timestamp for each partition
//            //If there is no corresponding offset for a timestamp in a partition it will return null and we'll leave the offset as set above at latest
//            Map<TopicPartition, OffsetAndTimestamp> partitionOffsetForTimestamp = consumer.offsetsForTimes(query);
//            for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : partitionOffsetForTimestamp.entrySet()) {
//                if (entry.getValue() != null) {
//                    //If we have an offset that corresponds to a timestamp then reset the offset for partition
//                    consumer.seek(entry.getKey(), entry.getValue().offset());
//                    LOG.info("Setting offset for partition " + entry.getKey().partition() + " to " + entry.getValue().offset());
//                } else {
//                    LOG.info("Leaving offset for partition " + entry.getKey().partition() + " to Latest");
//                }
//            }
//        }
            boolean consume = true;
            long timeoutTime = Instant.now().plusSeconds(targetNbSecInt).toEpochMilli(); //default to 30 seconds
            int nbFound = 0;

            List<Condition> conditions = new ArrayList<>();
            while (consume) {
                LOG.debug("Start Poll.");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(targetNbSecInt));
                LOG.debug("End Poll.");
                if (Instant.now().toEpochMilli() > timeoutTime) {
                    LOG.debug("Timed out searching for record");
                    consumer.wakeup(); //exit
                }
                //Now for each record in the batch of records we got from Kafka
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        LOG.debug("Found matching record " + record.topic() + " " + record.partition() + " " + record.offset());
                        LOG.debug("  " + record.key() + " | " + record.value());
                        JSONObject messageJSON = new JSONObject();
                        JSONObject recordJSON = new JSONObject(record.value());
                        messageJSON.put("key", record.key());
                        messageJSON.put("value", recordJSON);
                        messageJSON.put("offset", record.offset());
                        messageJSON.put("partition", record.partition());
                        resultJSON.put(messageJSON);
                        nbFound++;
                        if (nbFound >= targetNbEventsInt) {
                            consume = false;  //exit the consume loop
                            consumer.wakeup(); //takes effect on the next poll loop so need to break.
                            break; //if we've found a match, stop looping through the current record batch
                        }

                    } catch (Exception ex) {
                        //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                        //but we don't want to trigger the catch block for Kafka consumption
                        LOG.error(ex, ex);
                    }
                }
            }
            serviceREST.setResponseHTTPBody(resultJSON.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_SEEKKAFKA);
        } catch (WakeupException e) {
            LOG.debug("Kafka Wake UP Exception.", e);
            serviceREST.setResponseHTTPBody(resultJSON.toString());
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_SEEKKAFKA);
            //Ignore
        } catch (Exception ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKKAFKA);
            message.setDescription(message.getDescription().replace("%EX%", ex.toString()));
            LOG.debug(ex, ex);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }

        serviceREST.setResponseHTTPBodyContentType(AppServiceService.guessContentType(serviceREST, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));

        result.setItem(serviceREST);
        message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", AppService.METHOD_KAFKASEEK));
        message.setDescription(message.getDescription().replace("%TOPIC%", topic));
        result.setResultMessage(message);
        return result;
    }

}
