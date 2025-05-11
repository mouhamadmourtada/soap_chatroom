package com.example.chat;

import com.example.chat.client.ChatService;
import com.example.chat.client.ChatServiceImplService;

import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        System.out.println("Démarrage du client SOAP ChatRoom...");
        ChatServiceImplService service = new ChatServiceImplService();
        ChatService port = service.getChatServiceImplPort();

        Scanner scanner = new Scanner(System.in);
        String username = "";
        
        System.out.print("Entrez votre nom d'utilisateur : ");
        username = scanner.nextLine();
        
        boolean running = true;
        while (running) {
            System.out.println("\nOptions:");
            System.out.println("1. Envoyer un message");
            System.out.println("2. Voir tous les messages");
            System.out.println("3. Quitter");
            System.out.print("Votre choix : ");
            
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    System.out.print("Message : ");
                    String message = scanner.nextLine();
                    port.sendMessage(username, message);
                    System.out.println("Message envoyé");
                    break;
                case "2":
                    List<String> messages = port.getMessages();
                    System.out.println("\nMessages reçus :");
                    if (messages.isEmpty()) {
                        System.out.println("Aucun message dans la chatroom.");
                    } else {
                        for (String msg : messages) {
                            System.out.println(msg);
                        }
                    }
                    break;
                case "3":
                    running = false;
                    System.out.println("Au revoir !");
                    break;
                default:
                    System.out.println("Option non valide, veuillez réessayer.");
            }
        }
        
        scanner.close();
    }
}
