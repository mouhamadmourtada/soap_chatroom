package com.example.chat;

import jakarta.jws.WebService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebService(endpointInterface = "com.example.chat.ChatService")
public class ChatServiceImpl implements ChatService {

    private static final List<String> messages = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void sendMessage(String user, String message) {
        messages.add(user + ": " + message);
    }

    @Override
    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }
}
