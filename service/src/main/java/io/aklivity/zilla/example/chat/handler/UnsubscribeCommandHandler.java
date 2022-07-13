/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat.handler;

import org.apache.kafka.streams.KeyValue;

import io.aklivity.zilla.example.chat.model.Command;
import io.aklivity.zilla.example.chat.model.UnsubscribeCommand;

public class UnsubscribeCommandHandler implements CommandHandler
{
    @Override
    public KeyValue handle(Command command)
    {
        final UnsubscribeCommand unsubscribeCommand = (UnsubscribeCommand) command;
        final String userId = unsubscribeCommand.getUserId();
        final String channelId = unsubscribeCommand.getChannelId();

        return new KeyValue(String.format("%sZ%s", userId, channelId), null);
    }
}
