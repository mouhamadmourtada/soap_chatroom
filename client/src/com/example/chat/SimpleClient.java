package com.example.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client SOAP simple qui communique avec le serveur ChatService à l'aide
 * de requêtes HTTP directes (sans dépendances à des bibliothèques JAX-WS).
 */
public class SimpleClient {
    private static final String ENDPOINT_URL = "http://localhost:8080/chat";
    private static final String SOAP_ACTION = "";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String username = "";
        
        System.out.println("=== Chat Client SOAP ===");
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
            
            try {
                switch (choice) {
                    case "1":
                        System.out.print("Message : ");
                        String message = scanner.nextLine();
                        sendMessage(username, message);
                        System.out.println("Message envoyé avec succès.");
                        break;
                    case "2":
                        String[] messages = getMessages();
                        System.out.println("\nMessages dans la chatroom :");
                        if (messages.length == 0) {
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
            } catch (Exception e) {
                System.err.println("Erreur : " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * Envoie un message au service SOAP
     */
    private static void sendMessage(String user, String message) throws Exception {
        String soapRequest = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:chat=\"http://chat.example.com/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <chat:sendMessage>\n" +
            "         <arg0>" + escapeXml(user) + "</arg0>\n" +
            "         <arg1>" + escapeXml(message) + "</arg1>\n" +
            "      </chat:sendMessage>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
        
        sendSoapRequest(soapRequest);
    }
    
    /**
     * Récupère tous les messages du service SOAP
     */
    private static String[] getMessages() throws Exception {
        String soapRequest = 
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:chat=\"http://chat.example.com/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <chat:getMessages/>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
        
        String response = sendSoapRequest(soapRequest);
        return parseMessages(response);
    }
    
    /**
     * Envoie une requête SOAP et retourne la réponse
     */
    private static String sendSoapRequest(String soapRequest) throws Exception {
        URL url = new URL(ENDPOINT_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Configuration de la connexion
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        connection.setRequestProperty("SOAPAction", SOAP_ACTION);
        connection.setDoOutput(true);
        
        // Envoi de la requête
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = soapRequest.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        // Lecture de la réponse
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        return response.toString();
    }
    
    /**
     * Parse la réponse XML pour extraire les messages
     */
    private static String[] parseMessages(String soapResponse) {
        // Méthode simple pour extraire les messages de la réponse SOAP
        String startTag = "<return>";
        String endTag = "</return>";
        
        java.util.List<String> messages = new java.util.ArrayList<>();
        int currentIndex = 0;
        
        while (true) {
            int startIndex = soapResponse.indexOf(startTag, currentIndex);
            if (startIndex == -1) break;
            
            int endIndex = soapResponse.indexOf(endTag, startIndex + startTag.length());
            if (endIndex == -1) break;
            
            String message = soapResponse.substring(startIndex + startTag.length(), endIndex);
            messages.add(message);
            
            currentIndex = endIndex + endTag.length();
        }
        
        return messages.toArray(new String[0]);
    }
    
    /**
     * Échappe les caractères XML spéciaux
     */
    private static String escapeXml(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
