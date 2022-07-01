/*
 * Copyright 2021-2022 Aklivity. All rights reserved.
 */
package io.aklivity.zilla.example.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings({"HideUtilityClassConstructor"})
public class ChatServiceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
