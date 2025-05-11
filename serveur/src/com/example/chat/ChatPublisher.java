package com.example.chat;

import jakarta.xml.ws.Endpoint;

public class ChatPublisher {
    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8080/chat", new ChatServiceImpl());
        System.out.println("Chat SOAP service is running on http://localhost:8080/chat?wsdl");
    }
}
