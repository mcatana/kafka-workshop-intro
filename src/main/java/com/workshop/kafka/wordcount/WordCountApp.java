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
import java.util.concurrent.CountDownLatch;

public class WordCountApp {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-app-key2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.STATE_DIR_CONFIG, "F:\\BigData\\Kafka\\Projects\\statestore");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, String> source = builder.stream("words_input");
        final KGroupedStream<String, String> groupedStream = source
                .flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\s+")))
                .selectKey((key, word) -> word)
                .groupByKey();

        KTable<String, Long> countWords = groupedStream.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("word-count-new")
                .withValueSerde(Serdes.Long()));
        countWords.toStream().print(Printed.toSysOut());
        countWords.toStream().to("words_output", Produced.with(Serdes.String(), Serdes.Long()));



        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        System.out.println(topology.describe());
        streams.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        ReadOnlyKeyValueStore<String, Long> keyValueStore =
                streams.store("word-count-new", QueryableStoreTypes.keyValueStore());



        while (true) {
            // Get value by key
            System.out.println("count for alice:" + keyValueStore.get("alice"));
            System.out.println("count for wonder:" + keyValueStore.get("wonder"));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("all-values");
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