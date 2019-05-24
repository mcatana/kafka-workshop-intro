
**Movielens - create topics and import data**

Download the movies.csv and ratings.csv from https://github.com/academyofdata/inputs

**Note**: run the Kafka commands from your installation of confluent (ex: /opt/confluent) or add the full install path in the commands (ex: /opt/confluent/bin/kafka-topics ...)

1. Movies:

1.1 Create topic *movies_avro*


      ./bin/kafka-topics --create \
                --zookeeper localhost:2181 \
                --replication-factor 1 \
                --partitions 2 \
                --topic movies

1.2 Import csv to topic (change *inputs_path* to the folder where the csv is downloaded)

    cat /inputs_path/movies.csv | ./bin/kafka-console-producer --broker-list localhost:9092 --topic movies


1.3 Change to avro from ksql:

  Open ksql and set auto.offset.reset to earliest:

    LOG_DIR=./ksql_logs  ./bin/ksql
    SET 'auto.offset.reset'='earliest';

  Then run:

    CREATE STREAM movies_delimited(movieid STRING, title STRING, genre STRING, year STRING) WITH (KAFKA_TOPIC='movies', VALUE_FORMAT='DELIMITED');

    CREATE STREAM movies_avro_stream \
    WITH (KAFKA_TOPIC = 'movies_avro', VALUE_FORMAT='AVRO', PARTITIONS=2) AS
    SELECT movieid,title,genres,year FROM movies_delimited \
    PARTITION BY movieid;


2. Users


2.1 Create topic *users_avro*

    ./bin/kafka-topics --create \
              --zookeeper localhost:2181 \
              --replication-factor 1 \
              --partitions 2 \
              --topic users

2.2 Import csv to topic (change *inputs_path* to the folder where the csv is downloaded)

    cat /inputs_path/users.csv | ./bin/kafka-console-producer --broker-list localhost:9092 --topic users

2.3 Change to avro:

    CREATE STREAM users_delimited(uid STRING, gender STRING, age STRING, occupation STRING, zip STRING) WITH (KAFKA_TOPIC='users', VALUE_FORMAT='DELIMITED');

    CREATE STREAM users_avro_stream \
    WITH (KAFKA_TOPIC = 'users_avro', VALUE_FORMAT='AVRO', PARTITIONS=2) AS
    SELECT uid, gender, age, occupation, zip FROM users_delimited \
    PARTITION BY uid;


3. Ratings - generate data

3.1 Create topic *ratings_avro*

./bin/kafka-topics --create \
          --zookeeper localhost:2181 \
          --replication-factor 1 \
          --partitions 2 \
          --topic ratings_avro


3.2 Run the generator from command line:

Notes:
- the **ratings_avro** file is the one found in the project path resources folder (ex: kafka-workshop-intro/src/main/resources/ratings.avro). Change the *schema_path* to point to the location of ratings_avro.

From */opt/confluent* run:

./bin/ksql-datagen schema=/schema_path/ratings.avro format=avro topic=ratings_avro key=rmid maxInterval=5000

The generator will create rating records for the ratings_avro topic every 5000 milliseconds.
