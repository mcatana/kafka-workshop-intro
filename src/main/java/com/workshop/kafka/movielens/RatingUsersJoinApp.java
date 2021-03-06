package com.workshop.kafka.movielens;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Join Ratings (KStream) and Users (KTable)
 *
 * Notes:
 *
 * Before running this app:
 * - Change prefix property in config.properties.
 *   It will be used to create a unique application id for the Kafka cluster
 *
 * - Make sure you have the input topics in format avro: users_avro, ratings_avro
 * - Create output topic: prefix-ratings-join
 */

public class RatingUsersJoinApp {
    private static ResourceBundle rb = ResourceBundle.getBundle("config");

    public static void main(String[] args) {
        final String bootstrapServer = rb.getString("bootstrapServer");
        final String configPrefix =  rb.getString("prefix");
        final String schemaRegistry = rb.getString("schemaRegistry");
        final String applicationId = rb.getString("prefix") + "-rating-user-join-app";
        System.out.println("Starting app - configPrefix: " + configPrefix + ", bootstrapServer: " + bootstrapServer);

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        //table for users
        KTable<String, GenericRecord> usersTable = builder.table("users_avro",
                Consumed.with(Topology.AutoOffsetReset.EARLIEST));

        //stream for ratings
        KStream<String, GenericRecord> userRatingsStream = builder.stream("ratings_avro");
        userRatingsStream.print(Printed.toSysOut());

        //ratings - change key to ruid
        KStream<String, GenericRecord> userRatingsStreamKey = userRatingsStream.map(
              (key, record) -> KeyValue.pair(record.get("ruid").toString(),record));
        userRatingsStreamKey.print(Printed.toSysOut());

        //join ratings and users (by key userid)
        KStream<String, String> joinStream = userRatingsStreamKey.join(usersTable,
                (rating, user) -> "Rating=" + rating + ",user=[" + user + "]");


        joinStream.print(Printed.toSysOut());
        joinStream.to(configPrefix+"-ratings-join", Produced.with(Serdes.String(), Serdes.String()));


        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        System.out.println(topology.describe());
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }


}
