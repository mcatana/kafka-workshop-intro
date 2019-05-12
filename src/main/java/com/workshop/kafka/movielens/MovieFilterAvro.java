package com.workshop.kafka.movielens;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Properties;

/**
 * This app has 2 parts:
 * 1. Filter and count movies by year
 * 2. Classroom exercise: count the number of movies from genre Comedy
 *
 * Notes:
 *
 * The outputs of this app are only printed to console, no output topics were created.
 * Before running this app:
 * - Change prefix property in config.properties.
 *   It will be used to create a unique application id for the Kafka cluster
 *
 * - Make sure you have a topic in format avro: movies_avro
 *
 */
public class MovieFilterAvro {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "movie-filter-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, GenericRecord> moviesStream = builder.stream("movies_avro");
        final KStream<String, GenericRecord> filteredMovies = moviesStream.filter((key, record) ->
               record.get("YEAR").toString().equals("1997")
        );


        final KTable<String,Long> countByYear = filteredMovies.groupBy((key, record) -> record.get("YEAR").toString())
                .count();
        countByYear.toStream().print(Printed.toSysOut());

        //TODO - class exercise - change the code to number of movies from genre Comedy
        //NOTE: Be aware that after changing the code, before re-running the app,
        //you need to reset the app and clean the local state store.
        //Otherwise you can use a new application id.
        filteredMovies.print(Printed.toSysOut());
        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        System.out.println(topology.describe());

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
