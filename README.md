Introduction to Kafka Streams

**Project setup**

1. git clone https://github.com/mcatana/kafka-workshop-intro

2. Build with Maven:

  - cd /kafka-workshop-intro
  - mvn clean install -Dcheckstyle.skip

3. Import in Eclipse

  3.1 Go to File | Import | Maven - Existing Maven Projects, hit Next.
  	In the Root Directory - Browse to the directory where you have cloned the project (ex kafka-workshop-intro) and select it.
  	Under the Root Directory, in the list of Projects /kafka-workshop-intro/pom.xml is by default selected. Click Finish.
    The project will be visible in the left side. Select the project name, right click - Checkstyle - Deactivate checkstyle

  3.2 How to run the applications

  From the menu - Run | Run configurations | Add a new Java Application:
    - Name field - add the name you want, ex WordsUppercase
    - Project - kafka-workshop is already selected
    - Main class - select the class you want to run, ex WordsUppercaseApp.
    - Hit Apply
    - You can now run the app from Run Configurations or from the toolbar | Run shortcut.





4. Import in Intellij IDEA

  4.1. Go to File - New - Project from existing sources.
     In the File Explorer go to the path where you have the project (kafka-workshop-intro) and select pom.xml, click Ok.
     In the wizard Import projects from Maven click Next, then:
     - Select Maven project to import - select com.workshop.kafka:kafka-workshop:1.0-SNAPSHOT and click Next
     - Please select project SDK - select your Java 1.8 JDK home path and click Next
    - Please enter a name to create a new ItelliJ IDEA project - default values are completed for project name and project file location, click Finish

  4.2. You can run each app in the following way:

  - Select Run | Edit Configurations from the main menu
  - In the new window, from the toolbar click + (Add new configuration) and select Application
  - Specify the name in the Name field (ex WordsUppercase)
  - In the Main class - select the class you want to run and click ok
  - Apply the changes and close the dialog
  Note: If you want to run multiple instances of the same application, check "Allow running in parallel"




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
If not already created on your Kafka installation, check the movielensImport.md file for instructions.

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
                --config cleanup.policy=compact \
                --config segment.ms=100 \
                --replication-factor 1 \
                --partitions 2 \
                --topic prefix-movie-count-year

- consume from output topic:


      ./bin/kafka-console-consumer --bootstrap-server localhost:9092 \
              --topic prefix-movie-count-year \
              --from-beginning \
              --formatter kafka.tools.DefaultMessageFormatter \
              --property print.key=true \
              --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
              --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer





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
              --topic prefix-ratings-by-movie-count


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
