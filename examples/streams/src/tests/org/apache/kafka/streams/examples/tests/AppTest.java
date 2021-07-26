package org.apache.kafka.streams.examples.tests;

import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Properties;

public class AppTest {
    static String INPUT_TOPIC = "input";
    static String INPUT_TOPIC_2 = "input2";
    static String OUTPUT_TOPIC = "output";
    ForeachAction<String, String> print = (x, y) -> System.out.println("key: " + x + ", value: " + y);
    private TopologyTestDriver td;
    private Topology topology;
    private final Properties config;

    public AppTest() {

        config = new Properties();
        config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "StreamTest");
        config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "foo:1234");
        config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    }

    @After
    public void tearDown() {
        td.close();
    }

    @Test
    public void StreamVsTable() {

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> creditRequest$ = builder.stream(INPUT_TOPIC);
        KTable<String, String> creditRequestTable$ = creditRequest$.toTable();
        // creditRequestTable$.mapValues(s -> s).toStream().print(Printed.<String, String>toSysOut().withLabel("Table"));
        creditRequest$.groupByKey().count().toStream().peek((s, aLong) -> System.out.println(s + " => " + aLong));
        creditRequest$.print(Printed.<String, String>toSysOut().withLabel("Stream"));

        topology = builder.build();
        td = new TopologyTestDriver(topology, config);

        TestInputTopic<String, String> creditRequestTopic = td.createInputTopic(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        td.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer());
        creditRequestTopic.pipeInput("1", "Parker Co.");
        creditRequestTopic.pipeInput("2", "Pyromation");
        creditRequestTopic.pipeInput("1", "Parker Updated");
    }

    @Test
    public void TableToTableLeftJoin() {

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> creditRequest$ = builder.stream(INPUT_TOPIC);
        KStream<String, String> dealTeam$ = builder.stream(INPUT_TOPIC_2);
        // creditRequest$.peek(print).to(OUTPUT_TOPIC);

        // Table to Table Semantics allow SQL-like lookup behavior as the events come in.
        // Very similar to CombineLatest
        KTable<String, String> enrichedTable$ = dealTeam$.toTable();
        KTable<String, String> creditRequestTable$ = creditRequest$.toTable();
        creditRequestTable$.leftJoin(enrichedTable$, (left, right) -> left + ", " + right).toStream().peek(print);

        topology = builder.build();
        td = new TopologyTestDriver(topology, config);

        TestInputTopic<String, String> creditRequestTopic = td.createInputTopic(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        TestInputTopic<String, String> dealTeamTopic = td.createInputTopic(INPUT_TOPIC_2, Serdes.String().serializer(), Serdes.String().serializer());
        td.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer());
        creditRequestTopic.pipeInput("1", "Parker Co.");
        dealTeamTopic.pipeInput("1", "John Doe");
    }

    @Test
    public void TableToTableInnerJoin() {

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> creditRequest$ = builder.stream(INPUT_TOPIC);
        KStream<String, String> dealTeam$ = builder.stream(INPUT_TOPIC_2);
        // creditRequest$.peek(print).to(OUTPUT_TOPIC);

        // Table to Table Semantics allow SQL-like lookup behavior as the events come in.
        // Very similar to CombineLatest
        KTable<String, String> enrichedTable$ = dealTeam$.toTable();
        KTable<String, String> creditRequestTable$ = creditRequest$.toTable();
        creditRequestTable$.join(enrichedTable$, (left, right) -> left + ", " + right).toStream().peek(print);

        topology = builder.build();
        td = new TopologyTestDriver(topology, config);

        TestInputTopic<String, String> creditRequestTopic = td.createInputTopic(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        TestInputTopic<String, String> dealTeamTopic = td.createInputTopic(INPUT_TOPIC_2, Serdes.String().serializer(), Serdes.String().serializer());
        td.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer());
        creditRequestTopic.pipeInput("1", "Parker Co.");
        dealTeamTopic.pipeInput("1", "John Doe");
    }

    @Test
    public void StreamToStream() {

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> creditRequest$ = builder.stream(INPUT_TOPIC);
        KStream<String, String> dealTeam$ = builder.stream(INPUT_TOPIC_2);
        // creditRequest$.peek(print).to(OUTPUT_TOPIC);

        // Similar to Zip in
        KStream<String, String> enrichedTable$ = dealTeam$;
        KStream<String, String> creditRequestTable$ = creditRequest$;
        creditRequestTable$.join(enrichedTable$, (left, right) -> left + ", " + right, JoinWindows.of(Duration.ofDays(3))).peek(print);

        topology = builder.build();
        td = new TopologyTestDriver(topology, config);

        TestInputTopic<String, String> creditRequestTopic = td.createInputTopic(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        TestInputTopic<String, String> dealTeamTopic = td.createInputTopic(INPUT_TOPIC_2, Serdes.String().serializer(), Serdes.String().serializer());
        td.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer());
        creditRequestTopic.pipeInput("1", "Parker Co.");
        dealTeamTopic.pipeInput("1", "John Doe");

        // Should not emit because past window
        dealTeamTopic.pipeInput("1", "John Doe Blank", Instant.now().plus(Period.ofDays(3)));

        // Should emit because deal team and credit request came within 3 days
        creditRequestTopic.pipeInput("1", "John Doe Blank", Instant.now().plus(Period.ofDays(3)).plus(Period.ofDays(1)));
    }

    @Test
    public void LoadFromCache() {

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> creditRequest$ = builder.stream(INPUT_TOPIC);
        KStream<String, String> creditRequestTable$ = creditRequest$;
        creditRequestTable$.transform(() -> new CacheLookup()).peek(print);

        topology = builder.build();
        td = new TopologyTestDriver(topology, config);

        TestInputTopic<String, String> creditRequestTopic = td.createInputTopic(INPUT_TOPIC, Serdes.String().serializer(), Serdes.String().serializer());
        td.createOutputTopic(OUTPUT_TOPIC, Serdes.String().deserializer(), Serdes.String().deserializer());
        creditRequestTopic.pipeInput("1", "Parker Co.");
    }



    public class CacheLookup implements Transformer<String, String, KeyValue<String, String>> {

        @Override
        public void init(ProcessorContext processorContext) {
            System.out.println("Initialize Transformer");
        }

        @Override
        public KeyValue<String, String> transform(String s, String s2) {
            if (s.equals("1")) {
                return KeyValue.pair(s, "Enriched");
            }

            return KeyValue.pair(s, s2);
        }

        @Override
        public void close() {
            System.out.println("Close Transformer");
        }
    }
}