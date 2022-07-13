/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

import io.aklivity.zilla.example.chat.handler.CommandController;
import io.aklivity.zilla.example.chat.model.Command;
import io.aklivity.zilla.example.chat.model.SubscribeCommand;
import io.aklivity.zilla.example.chat.model.Subscription;
import io.aklivity.zilla.example.chat.model.User;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;

public class TopologyTest
{
    private static final String CHAT_COMMANDS_TOPIC = "commands";
    private static final String CHAT_USERS_TOPIC = "users";
    private static final String CHAT_SUBSCRIPTIONS_TOPIC = "subscriptions";

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, Command> commandsInTopic;
    private TestInputTopic<String, User> userInTopic;
    private TestOutputTopic<String, Subscription> subscriptionOutTopic;

    @BeforeEach
    public void setUp()
    {
        final StreamsBuilder builder = new StreamsBuilder();
        final ChatTopology processor = new ChatTopology(new CommandController());
        processor.chatSubscriptionsTopic = CHAT_SUBSCRIPTIONS_TOPIC;
        processor.chatCommandsTopic = CHAT_COMMANDS_TOPIC;
        processor.chatUsersTopic = CHAT_USERS_TOPIC;
        processor.buildPipeline(builder);
        final org.apache.kafka.streams.Topology topology = builder.build();

        final Properties props = new Properties();
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName());
        testDriver = new TopologyTestDriver(topology, props);

        commandsInTopic = testDriver.createInputTopic(CHAT_COMMANDS_TOPIC,
                new StringSerializer(), new JsonSerializer<>());
        userInTopic = testDriver.createInputTopic(CHAT_USERS_TOPIC,
                new StringSerializer(), new JsonSerializer<>());

        StringDeserializer keyDeserializer = new StringDeserializer();
        KafkaJsonDeserializer<Subscription> subscriptionDeserializer = new KafkaJsonDeserializer<>();
        subscriptionDeserializer.configure(Collections.emptyMap(), false);
        subscriptionOutTopic = testDriver.createOutputTopic(CHAT_SUBSCRIPTIONS_TOPIC,
                keyDeserializer, subscriptionDeserializer);
    }

    @AfterEach
    public void tearDown()
    {
        testDriver.close();
    }

    @Test
    public void shouldProcessCreateTaskCommand()
    {
        final Headers headers = new RecordHeaders(
                new Header[]{
                    new RecordHeader("domain-model", "SubscribeCommand".getBytes())
                });
        userInTopic.pipeInput(new TestRecord<>("7C114557589520964251508", User.builder()
                .id("7C114557589520964251508")
                .username("user1")
                .name("User")
                .status("online")
                .build(), headers));
        commandsInTopic.pipeInput(new TestRecord<>("subscriber1", SubscribeCommand.builder()
                .userId("7C114557589520964251508")
                .channelId("channel1")
                .build(), headers));

        List<KeyValue<String, Subscription>> subscriptions = subscriptionOutTopic.readKeyValuesToList();
        assertEquals(1, subscriptions.size());
        assertNotNull(subscriptions.get(0).value);
    }
}
