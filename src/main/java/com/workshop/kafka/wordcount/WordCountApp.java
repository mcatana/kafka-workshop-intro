package com.workshop.kafka.wordcount;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
/**
 * Word count - stateful query example using Kafka Streams DSL
 * Before running this app you must:
 * 1. Modify in config.properties:
 *    - host and port for your Kafka cluster
 *    bootstrapServer=hostname:port
 *
 *    - application prefix to be used when creating the application id
 *
 * 2. Create input and output topic (if not already created):
 *
 * #Input topic
 * 		  ./bin/kafka-topics --create \
 *           --zookeeper localhost:2181 \
 *           --replication-factor 1 \
 *           --partitions 2 \
 *           --topic prefix-words-input
 *
 * # Output topic
 *   ./bin/kafka-topics --create \
 *           --zookeeper localhost:2181 \
 *           --config cleanup.policy=compact \
 * 	         --config segment.ms=100 \
 *           --replication-factor 1 \
 *           --partitions 2 \
 *           --topic prefix-wordcount-output
 */
public class WordCountApp {

    private static ResourceBundle rb = ResourceBundle.getBundle("config");

    public static void main(String[] args) {


        final String bootstrapServer = rb.getString("bootstrapServer");
        final String configPrefix =  rb.getString("prefix");

        System.out.println("Starting app - configPrefix: " + configPrefix + ", bootstrapServer: " + bootstrapServer);

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, configPrefix + "-wordcount-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream(configPrefix+"-words-input");
        final KGroupedStream<String, String> groupedStream = source
                .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\s+")))
                .selectKey((key, word) -> word)
                .groupByKey();

        KTable<String, Long> countWords = groupedStream.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("word-count")
                .withValueSerde(Serdes.Long()));

        KStream<String, Long> streamCounts = countWords.toStream();
        streamCounts.print(Printed.toSysOut());
        streamCounts.to(configPrefix+ "-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));

        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        //print created topology
        System.out.println(topology.describe());
        streams.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ReadOnlyKeyValueStore<String, Long> keyValueStore =
                streams.store("word-count", QueryableStoreTypes.keyValueStore());


        while (true) {
            // Get value by key
            System.out.println("State store local values - count for test:" + keyValueStore.get("test"));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("State store local values: ");
            printAllValues(keyValueStore);

        }
    }


    private static void printAllValues(ReadOnlyKeyValueStore<String, Long> keyValueStore){
        //Get the values for all of the keys available in this application instance
        KeyValueIterator<String, Long> range = keyValueStore.all();
        while (range.hasNext()) {
            KeyValue<String, Long> next = range.next();
            System.out.println("count for " + next.key + ": " + next.value);
        }
    }
}