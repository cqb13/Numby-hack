package cqb13.Numby.utils;

import meteordevelopment.meteorclient.events.Cancellable;

public class SendRawMessage extends Cancellable {
    private static final SendRawMessage INSTANCE = new SendRawMessage();

    public String message;

    public static SendRawMessage get(String message) {
        INSTANCE.setCancelled(false);
        INSTANCE.message = message;
        return INSTANCE;
    }
}