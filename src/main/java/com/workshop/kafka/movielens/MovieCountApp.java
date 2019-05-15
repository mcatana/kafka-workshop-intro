package com.workshop.kafka.movielens;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Count movies by year
 *
 * Before running this app:
 * - Change prefix property in config.properties.
 *   It will be used to create a unique application id for the Kafka cluster
 *
 * - Make sure you have a topic in format avro: movies_avro
 * - Create output topic: prefix-movie-count-year
 */
public class MovieCountApp {
    private static ResourceBundle rb = ResourceBundle.getBundle("config");

    public static void main(String[] args) {
        final String bootstrapServer = rb.getString("bootstrapServer");
        final String configPrefix =  rb.getString("prefix");
        final String schemaRegistry = rb.getString("schemaRegistry");
        final String applicationId = rb.getString("prefix") + "-movie-count-year-app";
        System.out.println("Starting app - configPrefix: " + configPrefix + ", bootstrapServer: " + bootstrapServer);

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, GenericRecord> moviesStream = builder.stream("movies_avro");

        final KTable<String, Long> countMovies = moviesStream
              .groupBy((key, record) -> record.get("YEAR").toString()).count();

        countMovies.toStream().print(Printed.toSysOut());
        countMovies.toStream().to(configPrefix+"-movie-count-year");
        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        //print the created topology
        System.out.println(topology.describe());
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
