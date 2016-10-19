package me.evgeni.messenger;

/**
 * Created by Vadim Goroshevsky
 * Copyright (c) 2016 FusionWorks. All rights reserved.
 */

public class Message {
    public String text;
    public SenderType type;

    public Message(String text, SenderType type) {
        this.text = text;
        this.type = type;
    }

    public enum SenderType {
        HOST, CLIENT, DEFAULT
    }
}
