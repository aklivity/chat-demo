/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.streams.KeyValue;
import org.springframework.stereotype.Component;

import io.aklivity.zilla.example.chat.model.Command;

@Component
public class CommandController
{
    private final Map<Class<? extends Command>, CommandHandler> handlers = new HashMap<>();

    public KeyValue handle(Command command)
    {
        Class<? extends Command> commandClass = command.getClass();
        while (commandClass != null && !handlers.containsKey(commandClass))
        {
            Class <?> superClass = commandClass.getSuperclass();
            if (superClass == null || superClass.equals(Object.class))
            {
                commandClass = null;
            }
            else
            {
                commandClass = (Class<? extends Command>) superClass;
            }
        }

        if (commandClass != null)
        {
            return handlers.get(commandClass).handle(command);
        }
        return null;
    }

    public void addHandler(Class<? extends Command> eventToHandle, CommandHandler handler)
    {
        handlers.put(eventToHandle, handler);
    }
}
