/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat.processor;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;

import io.aklivity.zilla.example.chat.model.Subscription;

public class SubscriptionSupplier implements ProcessorSupplier<String, Subscription, Void, Void>
{
    public SubscriptionSupplier()
    {
    }

    public Processor<String, Subscription, Void, Void> get()
    {
        return new SubscriptionProcessor();
    }

    class SubscriptionProcessor implements Processor<String, Subscription, Void, Void>
    {
        ProcessorContext context;

        SubscriptionProcessor()
        {
        }

        public void init(final ProcessorContext context)
        {
            this.context = context;
        }

        public void process(Record<String, Subscription> record)
        {
            final Subscription subscription = record.value();
            if (subscription != null)
            {
                final Headers headers = new RecordHeaders();
                headers.add("user-id", subscription.getUserId().getBytes());
                headers.add("channel-id", subscription.getChannelId().getBytes());
                Record<String, Subscription> newRecord = record.withHeaders(headers);
                this.context.forward(newRecord);
            }
        }
    }
}
