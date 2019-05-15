Introduction to Kafka Streams

**Before running the examples:**

You need to change the following properties in /resources/config.properties:
- Kafka host and port:  boostrapServer

   Ex: boostrapServer=localhost:9092
- Unique prefix to be used when creating the application id and topic names: prefix

  Ex: prefix=mc

For some examples, you need to create the input and output topics, as detailed
below.
**When running the commands for creating the topics, make sure you replace 'prefix'
with the one you configured.**

1. Wordcount

            1.1  WordFilterApp

            Create input and output topics. The names must be unique, so make sure you change the prefix for the topics to the one configured in app config.properties.

            Create the input topic:
               ./bin/kafka-topics --create \
              --zookeeper localhost:2181 \
              --replication-factor 1 \
              --partitions 2 \
              --topic prefix-words-input

            Create the output topic:
             ./bin/kafka-topics --create \
                --zookeeper localhost:2181 \
                --replication-factor 1 \
                --partitions 2 \
                --topic prefix-words-filtered

            Adding data to the input topic:

             ./bin/kafka-console-producer --broker-list localhost:9092 --topic prefix-words-input

            Consuming data from the output topic

             ./bin/kafka-console-consumer --bootstrap-server localhost:9092 \
            --topic prefix-words-filtered \
            --from-beginning \
            --formatter kafka.tools.DefaultMessageFormatter \
            --property print.key=true \
            --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
            --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer


            1.2 WordsUpperCaseApp

            Create the output topic. The input topic is the one you created previously at 1.1
                       (make sure you change the prefix for the topic name).

            Output topic:
            ./bin/kafka-topics --create \
                         --zookeeper localhost:2181 \
                         --replication-factor 1 \
                         --partitions 2 \
                         --topic prefix-words-uppercase



             1.3 WordCountApp
         	   Create the output topic. The input topic is the one you created previously at 1.1 (make sure you change the prefix for the topic name).

            Output topic:
                ./bin/kafka-topics --create \
                    --zookeeper localhost:2181 \
                    --config cleanup.policy=compact \
          	        --config segment.ms=100 \
                    --replication-factor 1 \
                    --partitions 2 \
                    --topic prefix-wordcount-output




2. Movielens

We will use topics in avro format: movies_avro, ratings_avro, users_avro.

After changing the code, in order the reprocess the topic from the start, you must do an application reset.

http://docs.confluent.io/current/streams/developer-guide.html#application-reset-tool


2.1 MovieFilterApp

This example has 2 parts:
- Filter and count movies for year 1997
- Classroom exercise: count the number of movies from genre Comedy

Notes:
the output of this app is only printed to console, no output topic was created.
be aware that after changing the code, before re-running the app, you need to reset the app and clean the local state store. Otherwise you can use a new application id.


2.2 MovieCountApp

This example counts all the movies by year.

Notes:
- before running this example, create the output topic:
prefix-movie-count-year (change the prefix to the one you added in config.properties):


      ./bin/kafka-topics --create \
                --zookeeper localhost:2181 \
                --replication-factor 1 \
                --partitions 2 \
                --topic prefix-movie-count-year

- consume from output topic:


    ./bin/kafka-avro-console-consumer
        --bootstrap-server localhost:9092
        --topic prefix-movie-count-year




2.3 MovieRatingCountApp

This example counts ratings by movie id.

Notes:
- before running this example, create the output topic:
prefix--ratings-by-movie-count
 (change the prefix to the one you added in config.properties):


    ./bin/kafka-topics --create \
              --zookeeper localhost:2181 \
              --replication-factor 1 \
              --partitions 2 \
              --topic prefix--ratings-by-movie-count


2.4 RatingUsersJoinApp

  Join example: ratings (KStream) and users (KTable).

  Notes:
 - before running this example, create the output topic:
prefix-ratings-join
 (change the prefix to the one you added in config.properties):


    ./bin/kafka-topics --create \
              --zookeeper localhost:2181 \
              --replication-factor 1 \
              --partitions 2 \
              --topic prefix-ratings-join
