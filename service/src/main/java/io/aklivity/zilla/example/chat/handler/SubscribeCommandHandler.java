/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat.handler;

import org.apache.kafka.streams.KeyValue;

import io.aklivity.zilla.example.chat.model.Command;
import io.aklivity.zilla.example.chat.model.SubscribeCommand;
import io.aklivity.zilla.example.chat.model.Subscriber;

public class SubscribeCommandHandler implements CommandHandler
{
    @Override
    public KeyValue handle(Command command)
    {
        final SubscribeCommand subscribeCommand = (SubscribeCommand) command;
        final String userId = subscribeCommand.getUserId();
        final String channelId = subscribeCommand.getChannelId();
        return new KeyValue(String.format("%sZ%s", userId, channelId),
                Subscriber.builder()
                        .userId(userId)
                        .channelId(channelId)
                        .build());
    }
}
