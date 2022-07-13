/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat.handler;

import org.apache.kafka.streams.KeyValue;

import io.aklivity.zilla.example.chat.model.Command;

public interface CommandHandler
{
    KeyValue handle(Command command);
}
