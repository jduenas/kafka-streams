## Resetting a stream

- Ensure there is a way to replay events

```
https://kafka.apache.org/10/documentation/streams/developer-guide/app-reset-tool
https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Data+%28Re%29Processing+Scenarios

```

## Resetting the application:

```
# Ensure topics and application id are correct
kafka-streams-application-reset.sh --application-id streams-wordcount --input-topics streams-plaintext-input --bootstrap-servers kafka:9092 --zookeeper zookeeper:2181
```
