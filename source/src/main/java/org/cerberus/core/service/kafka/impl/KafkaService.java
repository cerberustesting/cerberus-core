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
package org.cerberus.core.service.kafka.impl;

import com.jayway.jsonpath.PathNotFoundException;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import java.io.IOException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.logging.log4j.Logger;
import org.cerberus.core.crud.entity.AppService;
import org.cerberus.core.crud.entity.AppServiceContent;
import org.cerberus.core.crud.entity.AppServiceHeader;
import org.cerberus.core.crud.entity.TestCaseExecution;
import org.cerberus.core.crud.entity.TestCaseStep;
import org.cerberus.core.crud.entity.TestCaseStepAction;
import org.cerberus.core.crud.factory.IFactoryAppService;
import org.cerberus.core.crud.factory.IFactoryAppServiceContent;
import org.cerberus.core.crud.factory.IFactoryAppServiceHeader;
import org.cerberus.core.crud.service.IAppServiceService;
import org.cerberus.core.crud.service.IParameterService;
import org.cerberus.core.engine.entity.MessageEvent;
import org.cerberus.core.engine.entity.MessageGeneral;
import org.cerberus.core.engine.execution.IRecorderService;
import org.cerberus.core.engine.gwt.IVariableService;
import org.cerberus.core.enums.MessageEventEnum;
import org.cerberus.core.enums.MessageGeneralEnum;
import org.cerberus.core.exception.CerberusEventException;
import org.cerberus.core.exception.CerberusException;
import org.cerberus.core.service.json.IJsonService;
import org.cerberus.core.service.kafka.IKafkaService;
import org.cerberus.core.service.proxy.IProxyService;
import org.cerberus.core.util.StringUtil;
import org.cerberus.core.util.answer.AnswerItem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.kafka.common.errors.SerializationException;
import org.cerberus.core.engine.entity.ExecutionLog;
import org.json.JSONException;

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
    IFactoryAppServiceContent factoryAppServiceContent;
    @Autowired
    IParameterService parameterService;
    @Autowired
    IFactoryAppService factoryAppService;
    @Autowired
    IAppServiceService appServiceService;
    @Autowired
    IProxyService proxyService;
    @Autowired
    IJsonService jsonService;
    @Autowired
    private IVariableService variableService;

    protected final Logger LOG = org.apache.logging.log4j.LogManager.getLogger(getClass());

    @Override
    public String getKafkaConsumerKey(String topic, String bootstrapServers) {
        return bootstrapServers + "|" + topic;
    }

    /**
     * This method will convert a json string to an Avro Object given the json
     * string and the Avro Schema. <br>
     * It has an issue on managing schema with multiple type possible. That
     * issue force the user to change a schema to a mono type in order to bypass
     * the error.<br>
     * Example:
     *
     * { <br>
     * "name": "productReferenceBU",<br>
     * "type": ["int","null"],<br>
     * "default": null<br>
     * }<br>
     *
     * has to be replaced by:
     *
     * {<br>
     * "name": "productReferenceBU",<br>
     * "type": "int",<br>
     * }<br>
     *
     * @param jsonString
     * @param schema
     * @return
     */
    private Object jsonToAvro(String jsonString, Schema schema) {
        try {
            DatumReader<Object> reader = new GenericDatumReader<Object>(schema);
            Object object = reader.read(null, DecoderFactory.get().jsonDecoder(schema, jsonString));

            if (schema.getType().equals(Schema.Type.STRING)) {
                object = ((Utf8) object).toString();
            }
            return object;
        } catch (IOException e) {
            throw new SerializationException(
                    String.format("Error deserializing json %s to Avro of schema %s", jsonString, schema), e);
        } catch (AvroRuntimeException e) {
            throw new SerializationException(
                    String.format("Error deserializing json %s to Avro of schema %s", jsonString, schema), e);
        }
    }

    /**
     * This method will convert a json string to an Avro Object given the json
     * string and the Avro Parsed Schema.<br>
     * This method is supposed to fix the previous bug but so far is not
     * integrated to Cerberus because it use the ParsedSchema.
     *
     * @param jsonString
     * @param schema
     * @return
     */
    private Object readFrom(String jsonString, ParsedSchema parsedSchema) {
        Schema schema = ((AvroSchema) parsedSchema).rawSchema();
        try {
            Object object = AvroSchemaUtils.toObject(jsonString, (AvroSchema) parsedSchema);
            if (schema.getType().equals(Schema.Type.STRING)) {
                object = ((Utf8) object).toString();
            }
            return object;
        } catch (IOException e) {
            throw new SerializationException(
                    String.format("Error deserializing json %s to Avro of schema %s", jsonString, schema), e);
        } catch (AvroRuntimeException e) {
            throw new SerializationException(
                    String.format("Error deserializing json %s to Avro of schema %s", jsonString, schema), e);
        }
    }

    @Override
    public AnswerItem<AppService> produceEvent(String topic, String key, String eventMessage,
            String bootstrapServers,
            List<AppServiceHeader> serviceHeader, List<AppServiceContent> serviceContent, String token, boolean activateAvro, String schemaRegistryURL,
            boolean isAvroEnableKey, String avroSchemaKey, boolean isAvroEnableValue, String avroSchemaValue, int timeoutMs) {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_PRODUCEKAFKA);
        AnswerItem<AppService> result = new AnswerItem<>();
        AppService serviceREST = factoryAppService.create("", AppService.TYPE_KAFKA, AppService.METHOD_KAFKAPRODUCE, "", "", "", "", "", "", "", "", "", "", "", "", true, "", "", false, "", false, "", false, "", null,
                "", null, "", null, null);

        // If token is defined, we add 'cerberus-token' on the http header.
        if (!StringUtil.isEmptyOrNull(token)) {
            serviceHeader.add(factoryAppServiceHeader.create(null, "cerberus-token", token, true, 0, "", "", null, "", null));
        }

        Properties props = new Properties();
        serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers, true, 0, "", "", null, "", null));
        serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true", true, 0, "", "", null, "", null));
        if (!activateAvro) {
            serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer", true, 0, "", "", null, "", null));
            serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer", true, 0, "", "", null, "", null));
        } else {
            if (isAvroEnableKey) {
                serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer", true, 0, "", "", null, "", null));
            } else {
                serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer", true, 0, "", "", null, "", null));
            }
            if (isAvroEnableValue) {
                serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer", true, 0, "", "", null, "", null));
            } else {
                serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer", true, 0, "", "", null, "", null));
            }
//            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
            serviceContent.add(factoryAppServiceContent.create(null, "schema.registry.url", schemaRegistryURL, true, 0, "", "", null, "", null));
        }
        // Setting timeout although does not seem to work fine as result on aiven is always 60000 ms.
        serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMs), true, 0, "", "", null, "", null));
        serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMs), true, 0, "", "", null, "", null));
        serviceContent.add(factoryAppServiceContent.create(null, ProducerConfig.MAX_BLOCK_MS_CONFIG, String.valueOf(timeoutMs), true, 0, "", "", null, "", null));

        for (AppServiceContent object : serviceContent) {
            if (object.isActive()) {
                props.put(object.getKey(), object.getValue());
            }
        }

        serviceREST.setServicePath(bootstrapServers);
        serviceREST.setKafkaTopic(topic);
        serviceREST.setKafkaKey(key);
        serviceREST.setServiceRequest(eventMessage);
        serviceREST.setHeaderList(serviceHeader);
        serviceREST.setContentList(serviceContent);

        int partition = -1;
        long offset = -1;
        KafkaProducer<Object, Object> producer = null;
        try {

            LOG.info("Open Producer : " + getKafkaConsumerKey(topic, bootstrapServers));
            producer = new KafkaProducer<>(props);

            if (activateAvro) {

                Schema.Parser parser = new Schema.Parser();
                Schema schemaValue;
                Schema schemaKey;

//                ParsedSchema toto ;
                ProducerRecord<Object, Object> record;
                if (isAvroEnableKey) {
                    if (isAvroEnableValue) {
                        schemaKey = parser.parse(avroSchemaKey);
                        schemaValue = parser.parse(avroSchemaValue);
                        record = new ProducerRecord<>(topic, jsonToAvro(key, schemaKey), jsonToAvro(eventMessage, schemaValue));
                    } else {
                        schemaKey = parser.parse(avroSchemaKey);
                        record = new ProducerRecord<>(topic, jsonToAvro(key, schemaKey), eventMessage);
                    }
                } else {
                    if (isAvroEnableValue) {
                        schemaValue = parser.parse(avroSchemaValue);
                        record = new ProducerRecord<>(topic, key, jsonToAvro(eventMessage, schemaValue));
                    } else {
                        record = new ProducerRecord<>(topic, key, eventMessage);
                    }
                }

//                GenericRecord eventMessageGeneric = new GenericData.Record(schema);
                for (AppServiceHeader object : serviceHeader) {
                    if (object.isActive()) {
                        record.headers().add(new RecordHeader(object.getKey(), object.getValue().getBytes()));
                    }
                }
                LOG.debug("Producing Kafka message (Avro enable) - topic : " + topic + " key : " + key + " message : " + eventMessage);
                RecordMetadata metadata = producer.send(record).get(); //Wait for a responses
                partition = metadata.partition();
                offset = metadata.offset();
                LOG.debug("Produced Kafka message (Avro enable) - topic : " + topic + " key : " + key + " partition : " + partition + " offset : " + offset);

            } else {
                ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, key, eventMessage);
                for (AppServiceHeader object : serviceHeader) {
                    if (object.isActive()) {
                        record.headers().add(new RecordHeader(object.getKey(), object.getValue().getBytes()));
                    }
                }
                LOG.debug("Producing Kafka message - topic : " + topic + " key : " + key + " message : " + eventMessage);
                RecordMetadata metadata = producer.send(record).get(); //Wait for a responses
                partition = metadata.partition();
                offset = metadata.offset();
                LOG.debug("Produced Kafka message - topic : " + topic + " key : " + key + " partition : " + partition + " offset : " + offset);

            }

            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_PRODUCEKAFKA);
        } catch (Exception ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_PRODUCEKAFKA);
            message.setDescription(message.getDescription().replace("%EX%", ex.toString() + " " + StringUtil.getExceptionCauseFromString(ex)));
            LOG.error(ex, ex);
        } finally {
            if (producer != null) {
                producer.flush();
                if (producer != null) {
                    try {
                        producer.close();
                    } catch (Exception e) {
                        LOG.error(e, e);
                    }
                }
                LOG.info("Closed Producer : " + getKafkaConsumerKey(topic, bootstrapServers));
            } else {
                LOG.info("Producer not opened : " + getKafkaConsumerKey(topic, bootstrapServers));
            }
        }

        serviceREST.setKafkaResponseOffset(offset);
        serviceREST.setKafkaResponsePartition(partition);

        serviceREST.setResponseHTTPBodyContentType(appServiceService.guessContentType(serviceREST, AppService.RESPONSEHTTPBODYCONTENTTYPE_JSON));

        result.setItem(serviceREST);
        message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", AppService.METHOD_KAFKAPRODUCE));
        message.setDescription(message.getDescription().replace("%TOPIC%", topic));
        message.setDescription(message.getDescription().replace("%PART%", String.valueOf(partition)));
        message.setDescription(message.getDescription().replace("%OFFSET%", String.valueOf(offset)));
        result.setResultMessage(message);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnswerItem<Map<TopicPartition, Long>> seekEvent(String topic, String bootstrapServers,
            List<AppServiceContent> kafkaProps, int timeoutMs) {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_SEARCHKAFKA);
        AnswerItem<Map<TopicPartition, Long>> result = new AnswerItem<>();

        KafkaConsumer consumer = null;

        try {

            Properties props = new Properties();
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers, true, 0, "", "", null, "", null));
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false", true, 0, "", "", null, "", null));
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10", true, 0, "", "", null, "", null));
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer", true, 0, "", "", null, "", null));
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer", true, 0, "", "", null, "", null));
            // Setting timeout although does not seem to work fine as result on aiven is always 60000 ms.
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMs), true, 0, "", "", null, "", null));
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMs), true, 0, "", "", null, "", null));
            kafkaProps.add(factoryAppServiceContent.create(null, ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMs), true, 0, "", "", null, "", null));

            for (AppServiceContent object : kafkaProps) {
                if (object.isActive()) {
                    props.put(object.getKey(), object.getValue());
                }
            }

            LOG.info("Open Consumer : " + getKafkaConsumerKey(topic, bootstrapServers));
            consumer = new KafkaConsumer<>(props);

            //Get a list of the topics' partitions
            List<PartitionInfo> partitionList = consumer.partitionsFor(topic);

            if (partitionList == null) {

                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKKAFKA);
                message.setDescription(message.getDescription().replace("%EX%", "Maybe Topic does not exist.").replace("%TOPIC%", topic).replace("%HOSTS%", bootstrapServers));

            } else {

                List<TopicPartition> topicPartitionList = partitionList.stream().map(info -> new TopicPartition(topic, info.partition())).collect(Collectors.toList());
                //Assign all the partitions to this consumer
                consumer.assign(topicPartitionList);
                consumer.seekToEnd(topicPartitionList); //default to latest offset for all partitions

                HashMap<TopicPartition, Long> valueResult = new HashMap<>();

                Map<TopicPartition, Long> partitionOffset = consumer.endOffsets(topicPartitionList);

                result.setItem(partitionOffset);

            }

        } catch (Exception ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKKAFKA);
            message.setDescription(message.getDescription().replace("%EX%", ex.toString() + " " + StringUtil.getExceptionCauseFromString(ex)).replace("%TOPIC%", topic).replace("%HOSTS%", bootstrapServers));
            LOG.debug(ex, ex);
        } finally {
            if (consumer != null) {
                consumer.close();
                LOG.info("Closed Consumer : " + getKafkaConsumerKey(topic, bootstrapServers));
            } else {
                LOG.info("Consumer not opened : " + getKafkaConsumerKey(topic, bootstrapServers));
            }
        }
        result.setResultMessage(message);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnswerItem<String> searchEvent(Map<TopicPartition, Long> mapOffsetPosition, String topic, String bootstrapServers,
            List<AppServiceHeader> serviceHeader, List<AppServiceContent> serviceContent, String filterPath, String filterValue, String filterHeaderPath, String filterHeaderValue,
            boolean activateAvro, String schemaRegistryURL, boolean avroEnableKey, boolean avroEnableValue, int targetNbEventsInt, int targetNbSecInt) {

        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEARCHKAFKA);
        AnswerItem<String> result = new AnswerItem<>();
        Instant date1 = Instant.now();

        JSONArray resultJSON = new JSONArray();

        KafkaConsumer consumer = null;
        int nbFound = 0;
        int nbEvents = 0;

        try {

            Properties props = new Properties();
            serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers, true, 0, "", "", null, "", null));
            serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false", true, 0, "", "", null, "", null));
            serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10", true, 0, "", "", null, "", null));

            if (!activateAvro) {
                serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer", true, 0, "", "", null, "", null));
                serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer", true, 0, "", "", null, "", null));
            } else {
                if (avroEnableKey) {
                    serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer", true, 0, "", "", null, "", null));
                } else {
                    serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer", true, 0, "", "", null, "", null));
                }
                if (avroEnableValue) {
                    serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer", true, 0, "", "", null, "", null));
                } else {
                    serviceContent.add(factoryAppServiceContent.create(null, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer", true, 0, "", "", null, "", null));
                }
//            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
                serviceContent.add(factoryAppServiceContent.create(null, "schema.registry.url", schemaRegistryURL, true, 0, "", "", null, "", null));
            }

            for (AppServiceContent object : serviceContent) {
                if (object.isActive()) {
                    props.put(object.getKey(), object.getValue());
                }
            }

            LOG.info("Open Consumer : " + getKafkaConsumerKey(topic, bootstrapServers));
            consumer = new KafkaConsumer<>(props);

            //Get a list of the topics' partitions
            List<PartitionInfo> partitionList = consumer.partitionsFor(topic);

            if (partitionList == null) {

                message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEARCHKAFKA);
                message.setDescription(message.getDescription().replace("%EX%", "Maybe Topic does not exist.").replace("%TOPIC%", topic).replace("%HOSTS%", bootstrapServers));

            } else {

                List<TopicPartition> topicPartitionList = partitionList.stream().map(info -> new TopicPartition(topic, info.partition())).collect(Collectors.toList());
                //Assign all the partitions to this consumer
                consumer.assign(topicPartitionList);
                // Setting each partition to correct Offset.
                for (Map.Entry<TopicPartition, Long> entry : mapOffsetPosition.entrySet()) {
                    consumer.seek(entry.getKey(), entry.getValue());
                    LOG.debug("Partition : " + entry.getKey().partition() + " set to offset : " + entry.getValue());
                }

                boolean consume = true;
                long timeoutTime = Instant.now().plusSeconds(targetNbSecInt).toEpochMilli(); //default to 30 seconds
                int pollDurationSec = 5;
                if (targetNbSecInt < pollDurationSec) {
                    pollDurationSec = targetNbSecInt;
                }

                while (consume) {
                    LOG.debug("Start Poll.");

                    if (!activateAvro) {

                        // NON AVRO VERSION
                        @SuppressWarnings("unchecked")
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(pollDurationSec));
                        LOG.debug("End Poll.");
                        if (Instant.now().toEpochMilli() > timeoutTime) {
                            LOG.debug("Timed out searching for record");
                            consumer.wakeup(); //exit
                        }
                        //Now for each record in the batch of records we got from Kafka
                        for (ConsumerRecord<String, String> record : records) {
                            try {
                                LOG.debug("New record " + record.topic() + " " + record.partition() + " " + record.offset());
                                LOG.debug("  " + record.key() + " | " + record.value());

                                // Parsing header.
                                JSONObject headerJSON = new JSONObject();
                                for (Header header : record.headers()) {
                                    String headerKey = header.key();
                                    String headerValue = new String(header.value());
                                    headerJSON.put(headerKey, headerValue);
                                }

                                boolean recordError = false;

                                // Parsing event message.
                                JSONObject recordJSON = new JSONObject();
                                try {
                                    recordJSON = new JSONObject(record.value());
                                } catch (JSONException ex) {
                                    LOG.warn("Failed to convert message to JSON Format : {}", record.value());
                                    LOG.warn(ex, ex);
                                    recordError = true;
                                }

                                // Complete event with headers.
                                JSONObject messageJSON = new JSONObject();
                                messageJSON.put("key", record.key());
                                if (recordError) {
                                    messageJSON.put("value", record.value());
                                } else {
                                    messageJSON.put("value", recordJSON);
                                }
                                messageJSON.put("offset", record.offset());
                                messageJSON.put("partition", record.partition());
                                messageJSON.put("header", headerJSON);

                                nbEvents++;

                                boolean match = isRecordMatch(record.value(), filterPath, filterValue, messageJSON.toString(), filterHeaderPath, filterHeaderValue);

                                if (match) {
                                    resultJSON.put(messageJSON);
                                    nbFound++;
                                    if (nbFound >= targetNbEventsInt) {
                                        consume = false;  //exit the consume loop
                                        consumer.wakeup(); //takes effect on the next poll loop so need to break.
                                        break; //if we've found a match, stop looping through the current record batch
                                    }
                                }

                            } catch (Exception ex) {
                                //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                                //but we don't want to trigger the catch block for Kafka consumption
                                LOG.error(ex, ex);
                            }
                        }

                    } else {

                        if (avroEnableKey) {

                            if (avroEnableValue) {

                                // AVRO KEY+VALUE VERSION
                                @SuppressWarnings("unchecked")
                                ConsumerRecords<GenericRecord, GenericRecord> recordsAvro = consumer.poll(Duration.ofSeconds(pollDurationSec));
                                LOG.debug("End Poll.");
                                if (Instant.now().toEpochMilli() > timeoutTime) {
                                    LOG.debug("Timed out searching for record");
                                    consumer.wakeup(); //exit
                                }
                                //Now for each record in the batch of records we got from Kafka
                                for (ConsumerRecord<GenericRecord, GenericRecord> record : recordsAvro) {
                                    try {
                                        LOG.debug("New record " + record.topic() + " " + record.partition() + " " + record.offset());
                                        LOG.debug("  " + record.key() + " | " + record.value());

                                        // Parsing header.
                                        JSONObject headerJSON = new JSONObject();
                                        for (Header header : record.headers()) {
                                            String headerKey = header.key();
                                            String headerValue = new String(header.value());
                                            headerJSON.put(headerKey, headerValue);
                                        }

                                        boolean recordError = false;

                                        // Parsing message.
                                        JSONObject recordJSON = new JSONObject();
                                        try {
                                            recordJSON = new JSONObject(record.value().toString());
                                        } catch (JSONException ex) {
                                            LOG.error(ex, ex);
                                            recordError = true;
                                        }

                                        // Parsing message.
                                        JSONObject keyJSON = new JSONObject();
                                        try {
                                            keyJSON = new JSONObject(record.key().toString());
                                        } catch (JSONException ex) {
                                            LOG.error(ex, ex);
                                            recordError = true;
                                        }

                                        // Complete event with headers.
                                        JSONObject messageJSON = new JSONObject();
                                        messageJSON.put("key", keyJSON);
                                        messageJSON.put("value", recordJSON);
                                        messageJSON.put("offset", record.offset());
                                        messageJSON.put("partition", record.partition());
                                        messageJSON.put("header", headerJSON);

                                        nbEvents++;

                                        boolean match = isRecordMatch(record.value().toString(), filterPath, filterValue, messageJSON.toString(), filterHeaderPath, filterHeaderValue);

                                        if (match) {
                                            resultJSON.put(messageJSON);
                                            nbFound++;
                                            if (nbFound >= targetNbEventsInt) {
                                                consume = false;  //exit the consume loop
                                                consumer.wakeup(); //takes effect on the next poll loop so need to break.
                                                break; //if we've found a match, stop looping through the current record batch
                                            }
                                        }

                                    } catch (Exception ex) {
                                        //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                                        //but we don't want to trigger the catch block for Kafka consumption
                                        LOG.error(ex, ex);
                                    }
                                }

                            } else {

                                // AVRO KEY VERSION
                                @SuppressWarnings("unchecked")
                                ConsumerRecords<GenericRecord, String> recordsAvro = consumer.poll(Duration.ofSeconds(pollDurationSec));
                                LOG.debug("End Poll.");
                                if (Instant.now().toEpochMilli() > timeoutTime) {
                                    LOG.debug("Timed out searching for record");
                                    consumer.wakeup(); //exit
                                }
                                //Now for each record in the batch of records we got from Kafka
                                for (ConsumerRecord<GenericRecord, String> record : recordsAvro) {
                                    try {
                                        LOG.debug("New record " + record.topic() + " " + record.partition() + " " + record.offset());
                                        LOG.debug("  " + record.key() + " | " + record.value());

                                        // Parsing header.
                                        JSONObject headerJSON = new JSONObject();
                                        for (Header header : record.headers()) {
                                            String headerKey = header.key();
                                            String headerValue = new String(header.value());
                                            headerJSON.put(headerKey, headerValue);
                                        }

                                        boolean recordError = false;

                                        // Parsing message.
                                        JSONObject recordJSON = new JSONObject();
                                        try {
                                            recordJSON = new JSONObject(record.value());
                                        } catch (JSONException ex) {
                                            LOG.error(ex, ex);
                                            recordError = true;
                                        }

                                        // Parsing message.
                                        JSONObject keyJSON = new JSONObject();
                                        try {
                                            keyJSON = new JSONObject(record.key().toString());
                                        } catch (JSONException ex) {
                                            LOG.error(ex, ex);
                                            recordError = true;
                                        }

                                        // Complete event with headers.
                                        JSONObject messageJSON = new JSONObject();
                                        messageJSON.put("key", keyJSON);
                                        messageJSON.put("value", record.value());
                                        messageJSON.put("offset", record.offset());
                                        messageJSON.put("partition", record.partition());
                                        messageJSON.put("header", headerJSON);

                                        nbEvents++;

                                        boolean match = isRecordMatch(record.value(), filterPath, filterValue, messageJSON.toString(), filterHeaderPath, filterHeaderValue);

                                        if (match) {
                                            resultJSON.put(messageJSON);
                                            nbFound++;
                                            if (nbFound >= targetNbEventsInt) {
                                                consume = false;  //exit the consume loop
                                                consumer.wakeup(); //takes effect on the next poll loop so need to break.
                                                break; //if we've found a match, stop looping through the current record batch
                                            }
                                        }

                                    } catch (Exception ex) {
                                        //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                                        //but we don't want to trigger the catch block for Kafka consumption
                                        LOG.error(ex, ex);
                                    }
                                }

                            }

                        } else {

                            // AVRO VALUE VERSION
                            @SuppressWarnings("unchecked")
                            ConsumerRecords<String, GenericRecord> recordsAvro = consumer.poll(Duration.ofSeconds(pollDurationSec));
                            LOG.debug("End Poll.");
                            if (Instant.now().toEpochMilli() > timeoutTime) {
                                LOG.debug("Timed out searching for record");
                                consumer.wakeup(); //exit
                            }
                            //Now for each record in the batch of records we got from Kafka
                            for (ConsumerRecord<String, GenericRecord> record : recordsAvro) {
                                try {
                                    LOG.debug("New record " + record.topic() + " " + record.partition() + " " + record.offset());
                                    LOG.debug("  " + record.key() + " | " + record.value());

                                    // Parsing header.
                                    JSONObject headerJSON = new JSONObject();
                                    for (Header header : record.headers()) {
                                        String headerKey = header.key();
                                        String headerValue = new String(header.value());
                                        headerJSON.put(headerKey, headerValue);
                                    }

                                    boolean recordError = false;

                                    // Parsing message.
                                    JSONObject recordJSON = new JSONObject();
                                    try {
                                        recordJSON = new JSONObject(record.value().toString());
                                    } catch (JSONException ex) {
                                        LOG.error(ex, ex);
                                        recordError = true;
                                    }

                                    // Complete event with headers.
                                    JSONObject messageJSON = new JSONObject();
                                    messageJSON.put("key", record.key());
                                    messageJSON.put("value", recordJSON);
                                    messageJSON.put("offset", record.offset());
                                    messageJSON.put("partition", record.partition());
                                    messageJSON.put("header", headerJSON);

                                    nbEvents++;

                                    boolean match = isRecordMatch(record.value().toString(), filterPath, filterValue, messageJSON.toString(), filterHeaderPath, filterHeaderValue);

                                    if (match) {
                                        resultJSON.put(messageJSON);
                                        nbFound++;
                                        if (nbFound >= targetNbEventsInt) {
                                            consume = false;  //exit the consume loop
                                            consumer.wakeup(); //takes effect on the next poll loop so need to break.
                                            break; //if we've found a match, stop looping through the current record batch
                                        }
                                    }

                                } catch (Exception ex) {
                                    //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                                    //but we don't want to trigger the catch block for Kafka consumption
                                    LOG.error(ex, ex);
                                }
                            }

                        }

                    }

                }
                result.setItem(resultJSON.toString());
                Instant date2 = Instant.now();
                Duration duration = Duration.between(date1, date2);
                message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_SEARCHKAFKA)
                        .resolveDescription("NBEVENT", String.valueOf(nbFound))
                        .resolveDescription("NBTOT", String.valueOf(nbEvents))
                        .resolveDescription("NBSEC", String.valueOf(duration.getSeconds()));

            }

        } catch (WakeupException e) {
            result.setItem(resultJSON.toString());
            Instant date2 = Instant.now();
            Duration duration = Duration.between(date1, date2);
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_SEARCHKAFKAPARTIALRESULT)
                    .resolveDescription("NBEVENT", String.valueOf(nbFound))
                    .resolveDescription("NBTOT", String.valueOf(nbEvents))
                    .resolveDescription("NBSEC", String.valueOf(duration.getSeconds()));
            //Ignore
        } catch (NullPointerException ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEARCHKAFKA);
            message.setDescription(message.getDescription().replace("%EX%", ex.toString()).replace("%TOPIC%", topic).replace("%HOSTS%", bootstrapServers));
            LOG.error(ex, ex);
        } catch (Exception ex) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEARCHKAFKA);
            message.setDescription(message.getDescription().replace("%EX%", ex.toString() + " " + StringUtil.getExceptionCauseFromString(ex)).replace("%TOPIC%", topic).replace("%HOSTS%", bootstrapServers));
            LOG.debug(ex, ex);
        } finally {
            if (consumer != null) {
                LOG.info("Closed Consumer : " + getKafkaConsumerKey(topic, bootstrapServers));
                consumer.close();
            } else {
                LOG.info("Consumer not opened : " + getKafkaConsumerKey(topic, bootstrapServers));
            }
        }

        result.setItem(resultJSON.toString());
        message.setDescription(message.getDescription().replace("%SERVICEMETHOD%", AppService.METHOD_KAFKASEARCH).replace("%TOPIC%", topic));
        result.setResultMessage(message);
        return result;
    }

    // Method to determine if the record match the filter criterias.
    private boolean isRecordMatch(String jsomEventMessage, String filterPath, String filterValue, String jsomMessage, String filterHeaderPath, String filterHeaderValue) {
        boolean match = true;

        if (!StringUtil.isEmptyOrNull(filterPath)) {
            String recordJSONfiltered = "";
            try {
                recordJSONfiltered = jsonService.getStringFromJson(jsomEventMessage, filterPath);
            } catch (PathNotFoundException ex) {
                //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                //but we don't want to trigger the catch block for Kafka consumption
                match = false;
                LOG.debug("Record discarded - Event Path not found.");
            } catch (Exception ex) {
                LOG.error(ex, ex);
            }
            LOG.debug("Filtered Event value : " + recordJSONfiltered);
            if (!recordJSONfiltered.equals(filterValue)) {
                match = false;
                LOG.debug("Record discarded - Event Value different.");
            }
        }

        if (!StringUtil.isEmptyOrNull(filterHeaderPath)) {
            String recordJSONfiltered = "";
            try {
                recordJSONfiltered = jsonService.getStringFromJson(jsomMessage, filterHeaderPath);
            } catch (PathNotFoundException ex) {
                //Catch any exceptions thrown from message processing/testing as they should have already been reported/dealt with
                //but we don't want to trigger the catch block for Kafka consumption
                match = false;
                LOG.debug("Record discarded - Message Path not found.");
            } catch (Exception ex) {
                LOG.error(ex, ex);
            }
            LOG.debug("Filtered Message value : " + recordJSONfiltered);
            if (!recordJSONfiltered.equals(filterHeaderValue)) {
                match = false;
                LOG.debug("Record discarded - Message Value different.");
            }
        }

        return match;
    }

    @Override
    public HashMap<String, Map<TopicPartition, Long>> getAllConsumers(List<TestCaseStep> mainExecutionTestCaseStepList, TestCaseExecution tCExecution) throws CerberusException {
        HashMap<String, Map<TopicPartition, Long>> tempKafka = new HashMap<>();
        AnswerItem<Map<TopicPartition, Long>> resultConsume = new AnswerItem<>();
        MessageEvent message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKALLTOPICS);

        for (TestCaseStep testCaseStep : mainExecutionTestCaseStepList) {
            for (TestCaseStepAction testCaseStepAction : testCaseStep.getActions()) {
                if (testCaseStepAction.getAction().equals(TestCaseStepAction.ACTION_CALLSERVICE)
                        && !testCaseStepAction.getConditionOperator().equals(TestCaseStepAction.CONDITIONOPERATOR_NEVER)) {

                    AnswerItem<AppService> localService = appServiceService.readByKeyWithDependency(testCaseStepAction.getValue1(), true);
                    if (localService.getItem() != null) {
                        if (localService.getItem().getType().equals(AppService.TYPE_KAFKA) && localService.getItem().getMethod().equals(AppService.METHOD_KAFKASEARCH)) {

                            try {

                                String decodedTopic = localService.getItem().getKafkaTopic();
                                AnswerItem<String> answerDecode = variableService.decodeStringCompletly(decodedTopic, tCExecution, null, false);
                                decodedTopic = answerDecode.getItem();
                                if (!(answerDecode.isCodeStringEquals("OK"))) {
                                    // If anything wrong with the decode --> we stop here with decode message in the action result.
                                    String field = "Kafka Topic of Service '" + localService.getItem().getService() + "'";
                                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKALLTOPICS)
                                            .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                    LOG.debug("Getting all consumers interupted due to decode '" + field + "'.");
                                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
                                    mes.setDescription(message.getDescription());
                                    throw new CerberusException(mes);
                                }

                                String decodedServicePath = localService.getItem().getServicePath();
                                answerDecode = variableService.decodeStringCompletly(decodedServicePath, tCExecution, null, false);
                                decodedServicePath = answerDecode.getItem();
                                if (!(answerDecode.isCodeStringEquals("OK"))) {
                                    // If anything wrong with the decode --> we stop here with decode message in the action result.
                                    String field = "Kafka Service Path of Service '" + localService.getItem().getService() + "'";
                                    message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKALLTOPICS)
                                            .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                    LOG.debug("Getting all consumers interupted due to decode '" + field + "'.");
                                    MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
                                    mes.setDescription(message.getDescription());
                                    throw new CerberusException(mes);
                                }

                                List<AppServiceHeader> headers = localService.getItem().getHeaderList();
                                // Decode Header List
                                List<AppServiceHeader> decodedHeaders = new ArrayList<>();
                                for (AppServiceHeader object : headers) {
                                    answerDecode = variableService.decodeStringCompletly(object.getKey(), tCExecution, null, false);
                                    object.setKey(answerDecode.getItem());
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Header Key " + object.getKey() + " of Service '" + localService.getItem().getService() + "'";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKALLTOPICS)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Getting all consumers interupted due to decode '" + field + "'.");
                                        MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
                                        mes.setDescription(message.getDescription());
                                        throw new CerberusException(mes);
                                    }

                                    answerDecode = variableService.decodeStringCompletly(object.getValue(), tCExecution, null, false);
                                    object.setValue(answerDecode.getItem());
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Header Value " + object.getKey() + " of Service '" + localService.getItem().getService() + "'";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKALLTOPICS)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Getting all consumers interrupted due to decode '" + field + "'.");
                                        MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
                                        mes.setDescription(message.getDescription());
                                        throw new CerberusException(mes);
                                    }
                                    decodedHeaders.add(object);
                                }

                                List<AppServiceContent> props = localService.getItem().getContentList();
                                // Decode Header List
                                List<AppServiceContent> decodedContent = new ArrayList<>();
                                for (AppServiceContent object : props) {
                                    answerDecode = variableService.decodeStringCompletly(object.getKey(), tCExecution, null, false);
                                    object.setKey(answerDecode.getItem());
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Header Key " + object.getKey() + " of Service '" + localService.getItem().getService() + "'";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKALLTOPICS)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Getting all consumers interupted due to decode '" + field + "'.");
                                        MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
                                        mes.setDescription(message.getDescription());
                                        throw new CerberusException(mes);
                                    }

                                    answerDecode = variableService.decodeStringCompletly(object.getValue(), tCExecution, null, false);
                                    object.setValue(answerDecode.getItem());
                                    if (!(answerDecode.isCodeStringEquals("OK"))) {
                                        // If anything wrong with the decode --> we stop here with decode message in the action result.
                                        String field = "Header Value " + object.getKey() + " of Service '" + localService.getItem().getService() + "'";
                                        message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CALLSERVICE_SEEKALLTOPICS)
                                                .resolveDescription("DESCRIPTION", answerDecode.getResultMessage().resolveDescription("FIELD", field).getDescription());
                                        LOG.debug("Getting all consumers interupted due to decode '" + field + "'.");
                                        MessageGeneral mes = new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND);
                                        mes.setDescription(message.getDescription());
                                        throw new CerberusException(mes);
                                    }

                                    decodedContent.add(object);
                                }

                                tCExecution.addExecutionLog(ExecutionLog.STATUS_INFO, "Seek Kafka topic '" + decodedTopic);
                                resultConsume = seekEvent(decodedTopic, decodedServicePath, decodedContent, parameterService.getParameterIntegerByKey("cerberus_callservice_timeoutms", tCExecution.getSystem(), 60000));

                                if (!(resultConsume.isCodeEquals(MessageEventEnum.ACTION_SUCCESS_CALLSERVICE_SEARCHKAFKA.getCode()))) {
                                    LOG.debug("TestCase interupted due to error when opening Kafka consume. " + resultConsume.getMessageDescription());
                                    tCExecution.addExecutionLog(ExecutionLog.STATUS_WARN, "TestCase interupted due to error when opening Kafka consume. " + resultConsume.getMessageDescription());
                                    throw new CerberusException(new MessageGeneral(MessageGeneralEnum.VALIDATION_FAILED_KAFKACONSUMERSEEK).resolveDescription("SERVICE", localService.getItem().getService())
                                            .resolveDescription("DETAIL", resultConsume.getMessageDescription()));
                                }
                                LOG.debug("Saving Map to key : " + getKafkaConsumerKey(localService.getItem().getKafkaTopic(), localService.getItem().getServicePath()));
                                tempKafka.put(getKafkaConsumerKey(decodedTopic, decodedServicePath), resultConsume.getItem());
                                tCExecution.addExecutionLog(ExecutionLog.STATUS_INFO, "Saving Map to key : " + getKafkaConsumerKey(localService.getItem().getKafkaTopic(), localService.getItem().getServicePath()) + " " + resultConsume.getItem().toString());

                            } catch (CerberusEventException ex) {
                                LOG.error(ex);
                            }

                        }
                    }

                }
            }
        }
        LOG.debug(tempKafka.size() + " consumers lastest offset retrieved.");
        tCExecution.addExecutionLog(ExecutionLog.STATUS_INFO, tempKafka.size() + " consumers lastest offset retrieved.");
        return tempKafka;
    }

}
