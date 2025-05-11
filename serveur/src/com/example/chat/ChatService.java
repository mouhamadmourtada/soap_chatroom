package com.example.chat;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import java.util.List;

@WebService
public interface ChatService {
    @WebMethod
    void sendMessage(String user, String message);

    @WebMethod
    List<String> getMessages();
}
