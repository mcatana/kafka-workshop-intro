package com.workshop.kafka.wordcount;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

/**
 * Words filter - example for stateless query using Kafka Streams DSL
 * Before running this app you must:
 * 1. Modify in config.properties:
 *    - host and port for your Kafka cluster
 *    bootstrapServer=hostname:port
 *
 *    - application prefix to be used when creating the application id
 *
 * 2. Create input and output topic (if not already created):
 *
 * 		  #Input topic
 * 		  ./bin/kafka-topics --create \
 *           --zookeeper localhost:2181 \
 *           --replication-factor 1 \
 *           --partitions 2 \
 *           --topic words-input
 *
 * # Create the output topic
 *   ./bin/kafka-topics --create \
 *           --zookeeper localhost:2181 \
 *           --replication-factor 1 \
 *           --partitions 2 \
 *           --topic words-filtered
 *
 */
public class WordsFilterApp {
    private static ResourceBundle rb = ResourceBundle.getBundle("config");

    public static void main(String[] args) {
        final String bootstrapServer = rb.getString("bootstrapServer");
        final String applicationId = rb.getString("prefix") + "-words-filter-app";

        System.out.println("Starting app id: " + applicationId + ", bootstrapServer: " + bootstrapServer);

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> sourceStream = builder.stream("words-input");

        final KStream<String,String> filteredStream = sourceStream
                //split by space
                .flatMapValues(value-> Arrays.asList(value.toLowerCase().split("\\s+")))
                //filter words that start with 'a'
                .filter((key,value)-> (value.toString().startsWith("a")));
        filteredStream.to("words-filtered", Produced.with(Serdes.String(), Serdes.String()));
        //print the output of the filtered stream in the console
        filteredStream.print(Printed.toSysOut());
        Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, props);
        //print created topology
        System.out.println(topology.describe());

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }
}
