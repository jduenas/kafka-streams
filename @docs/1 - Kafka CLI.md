## Consumer Group Management

```
# View Consumer Groups
kafka-consumer-groups.sh  --list --bootstrap-server localhost:9092


# View Offsets
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --all-groups --describe
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group <consumer-group> --describe

# Reset Offsets
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group streams-wordcount --topic streams-plaintext-input --reset-offsets --to-earliest --execute
```

## Create Topic

```
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic streams-plaintext-input
kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic streams-wordcount-output
```

## Delete topics

```
kafka-topics.sh --bootstrap-server localhost:9092 --topic streams-plaintext-input --delete
kafka-topics.sh --bootstrap-server localhost:9092 --topic streams-wordcount-output --delete
```

## Produce to Topic

```
kafka-console-producer.sh --broker-list localhost:9092 --topic streams-plaintext-input

# enter
kafka streams udemy
kafka data processing
kafka streams course
# Data is Published upon exit
```

## Start a consumer on the output topic

```
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic streams-wordcount-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer

kafka-console-consumer.sh --bootstrap-server localhost:9092 \
    --topic streams-colors-output \
    --from-beginning \
    --formatter kafka.tools.DefaultMessageFormatter \
    --property print.key=true \
    --property print.value=true \
    --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
    --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
``

## List topics
```

kafka-topics.sh --bootstrap-server kafka:9092 --list

```

## Running the example
```

java -cp org.apache.kafka.streams.examples.wordcount.WordCountDemo

```

```
