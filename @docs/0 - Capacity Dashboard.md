# Work

- Create a JDBC Key-Value State Store
  - Based on In-Memory Key-Value store of Kafka or RocksDB Implementation
  - Support multiple Instances
  - Test Windowing (deletes)

# Considerations

- Exactly Once Semantics will be important:
  - Add kafka-clients dependency
  - Producer Config: properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
  - Consumer Config: properties.put(StreamConfig.PROCESESING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)
- Can internal Kafka topics be created in the cluster? Yes
- Compacted Topics have no time based retention applicable
- Check how to connect locally

- What is the retention period for state stores?

- Join Requirements

```
The requirements for data co-partitioning are:

The input topics of the join (left side and right side) must have the same number of partitions.
All applications that write to the input topics must have the same partitioning strategy so that records with the same key are delivered to same partition number. In other words, the keyspace of the input data must be distributed across partitions in the same manner. This means that, for example, applications that use Kafka’s Java Producer API must use the same partitioner (cf. the producer setting "partitioner.class" aka ProducerConfig.PARTITIONER_CLASS_CONFIG), and applications that use the Kafka’s Streams API must use the same StreamPartitioner for operations such as KStream#to(). The good news is that, if you happen to use the default partitioner-related settings across all applications, you do not need to worry about the partitioning strategy.
```
