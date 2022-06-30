/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.aklivity.zilla.example.chat.model.Command;
import io.aklivity.zilla.example.chat.model.SubscribeCommand;
import io.aklivity.zilla.example.chat.model.Subscriber;
import io.aklivity.zilla.example.chat.model.Subscription;
import io.aklivity.zilla.example.chat.model.UnsubscribeCommand;
import io.aklivity.zilla.example.chat.model.User;
import io.aklivity.zilla.example.chat.processor.SubscriptionSupplier;
import io.aklivity.zilla.example.chat.serde.SerdeFactory;

@Component
public class ChatTopology
{
    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Command> commandSerde = SerdeFactory.commandSerde();
    private final Serde<User> userSerde = SerdeFactory.jsonSerdeFor(User.class, false);
    private final Serde<Subscriber> subscriberSerde = SerdeFactory.jsonSerdeFor(Subscriber.class, false);
    private final Serde<Subscription> subscriptionSerde = SerdeFactory.jsonSerdeFor(Subscription.class, false);
    @Value("${chat.subscriptions.topic}")
    String chatSubscriptionsTopic;
    @Value("${chat.commands.topic}")
    String chatCommandsTopic;
    @Value("${chat.users.topic}")
    String chatUsersTopic;

    public ChatTopology()
    {
    }

    @Autowired
    public void buildPipeline(StreamsBuilder streamBuilder)
    {
        final KTable<String, Subscriber> subscriberTable = streamBuilder.stream(this.chatCommandsTopic,
                Consumed.with(this.stringSerde, this.commandSerde))
                .map((key, value) ->
                {
                    if (value instanceof SubscribeCommand)
                    {
                        SubscribeCommand command = (SubscribeCommand)value;
                        final String userId = command.getUserId();
                        final String channelId = command.getChannelId();
                        return new KeyValue(String.format("%sZ%s", userId, channelId),
                                Subscriber.builder()
                                        .userId(userId)
                                        .channelId(channelId)
                                        .build());
                    }
                    else if (value instanceof UnsubscribeCommand)
                    {
                        UnsubscribeCommand command = (UnsubscribeCommand)value;
                        final String userId = command.getUserId();
                        final String channelId = command.getChannelId();
                        return new KeyValue(String.format("%sZ%s", userId, channelId), null);
                    }
                    else
                    {
                        return null;
                    }
                })
                .toTable(Materialized.<String, Subscriber, KeyValueStore<String, Subscriber>>
                                as("SUBSCRIBER-MV")
                        .withKeySerde(stringSerde)
                        .withValueSerde(subscriberSerde));

        final KTable<String, User> userTable = streamBuilder.stream(this.chatUsersTopic,
                Consumed.with(this.stringSerde, this.userSerde))
                .map((key, value) -> new KeyValue(value.getId(), value))
                .toTable(Materialized.<String, User, KeyValueStore<String, User>>
                    as("USER-MV")
                    .withKeySerde(stringSerde)
                    .withValueSerde(userSerde));

        String subscriptionProcessor = "SubscriptionProcessor";
        subscriberTable.join(userTable,
                        Subscriber::getUserId,
                        (subscriber, user) -> subscriber != null ?
                            Subscription.builder()
                            .userId(subscriber.getUserId())
                            .channelId(subscriber.getChannelId())
                            .name(user.getName())
                            .username(user.getUsername())
                            .status(user.getStatus())
                            .build() : null)
                        .toStream()
                        .process(new SubscriptionSupplier(), Named.as(subscriptionProcessor));

        Topology topologyBuilder = streamBuilder.build();
        topologyBuilder.addSink("SubscriptionSink",
                this.chatSubscriptionsTopic,
                this.stringSerde.serializer(),
                this.subscriptionSerde.serializer(),
                subscriptionProcessor);
    }
}
