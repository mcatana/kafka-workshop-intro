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

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Wordcount filter - example for stateless query using Kafka Streams DSL
 *
 */
public class WordsFilterApp {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "words-filter-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> sourceStream = builder.stream("words_input");
        sourceStream.print(Printed.toSysOut());
        final KStream<String,String> filteredStream = sourceStream
                //split by space
                .flatMapValues(value-> Arrays.asList(value.toLowerCase().split("\\s+")))
                //filter words that start with 'a'
                .filter((key,value)->value.toString().startsWith("a"));
        filteredStream.to("words-filtered", Produced.with(Serdes.String(), Serdes.String()));
        filteredStream.print(Printed.toSysOut());
        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        System.out.println(topology.describe());

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }
}
