package com.workshop.kafka.movielens;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

public class RatingUsersGlobalJoinApp {
    public static void main(String[] args) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "rating-user-global-join-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final StreamsBuilder builder = new StreamsBuilder();

        //join ratings and users

        //global ktable for users - replicated on each instance
        GlobalKTable<String, GenericRecord> usersGlobalTable = builder.globalTable("users_avro2");

        //stream for ratings
        KStream<String, GenericRecord> userRatingsStream = builder.stream("RATINGS_USER_STREAM_AVRO");

        userRatingsStream.print(Printed.toSysOut());
        //change key to RUID
//        KStream<String, GenericRecord> userRatingsStreamKey = userRatingsStream.map(
//                (key, record) -> KeyValue.pair(record.get("RUID").toString(),record));

        KStream<String, String> joinStream = userRatingsStream.join(usersGlobalTable,
                (key, value) -> key, /* map from the (key, value) of this stream to the key of the GlobalKTable */
                (rating, user) -> "Rating=" + rating + ",user=[" + user + "]");


        joinStream.print(Printed.toSysOut());
        joinStream.to("ratings_join", Produced.with(Serdes.String(), Serdes.String()));


        Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        System.out.println(topology.describe());
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
