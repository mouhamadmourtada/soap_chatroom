package com.example.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation HTTP du service de chat SOAP.
 * Cette classe fait les appels SOAP manuellement en utilisant des requêtes HTTP standard
 * au lieu d'utiliser les bibliothèques générées par wsimport.
 * Cela permet d'éviter les problèmes de compatibilité entre les namespaces javax et jakarta.
 *
 * @author Client Chat SOAP
 * @version 1.0
 */
public class ChatServiceImpl implements JakartaClient.ChatService {
    
    // URL du service SOAP, modifiable via un setter pour plus de flexibilité
    private String endpointUrl = "http://localhost:8080/chat";
    private static final String SOAP_ACTION = "";
    
    /**
     * Constructeur par défaut qui utilise l'URL par défaut (localhost:8080)
     */
    public ChatServiceImpl() {
        // Utiliser l'URL par défaut
    }
    
    /**
     * Constructeur permettant de spécifier une URL de service différente
     * 
     * @param serviceUrl L'URL du service SOAP
     */
    public ChatServiceImpl(String serviceUrl) {
        if (serviceUrl != null && !serviceUrl.isEmpty()) {
            this.endpointUrl = serviceUrl;
        }
    }
    
    /**
     * Modifie l'URL du service SOAP
     * 
     * @param serviceUrl La nouvelle URL du service
     */
    public void setEndpointUrl(String serviceUrl) {
        if (serviceUrl != null && !serviceUrl.isEmpty()) {
            this.endpointUrl = serviceUrl;
        }
    }
    
    /**
     * Récupère l'URL du service SOAP actuelle
     * 
     * @return L'URL du service
     */
    public String getEndpointUrl() {
        return this.endpointUrl;
    }
      /**
     * Envoie un message au service de chat
     * 
     * @param user Le nom d'utilisateur de l'expéditeur
     * @param message Le contenu du message
     * @throws RuntimeException Si une erreur survient lors de l'envoi
     */
    @Override
    public void sendMessage(String user, String message) {
        if (user == null || user.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide");
        }
        
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide");
        }
        
        try {
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
        } catch (java.net.ConnectException e) {
            throw new RuntimeException("Impossible de se connecter au serveur à l'adresse " + this.endpointUrl, e);
        } catch (java.net.UnknownHostException e) {
            throw new RuntimeException("Adresse du serveur introuvable : " + this.endpointUrl, e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi du message", e);
        }
    }
      /**
     * Récupère tous les messages du service de chat
     * 
     * @return Liste des messages au format "username: message"
     * @throws RuntimeException Si une erreur survient lors de la récupération
     */
    @Override
    public List<String> getMessages() {
        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:chat=\"http://chat.example.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <chat:getMessages/>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
            
            String response = sendSoapRequest(soapRequest);
            return parseMessages(response);
        } catch (java.net.ConnectException e) {
            throw new RuntimeException("Impossible de se connecter au serveur à l'adresse " + this.endpointUrl, e);
        } catch (java.net.UnknownHostException e) {
            throw new RuntimeException("Adresse du serveur introuvable : " + this.endpointUrl, e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des messages", e);
        }
    }
      /**
     * Envoie une requête SOAP et retourne la réponse
     * 
     * @param soapRequest La requête SOAP au format XML
     * @return La réponse du serveur au format XML
     * @throws Exception Si une erreur réseau ou de communication se produit
     */
    private String sendSoapRequest(String soapRequest) throws Exception {
        URL url = new URL(this.endpointUrl);
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
     * 
     * @param soapResponse Réponse SOAP brute au format XML
     * @return Liste des messages extraits
     */
    private List<String> parseMessages(String soapResponse) {
        // Méthode simple pour extraire les messages de la réponse SOAP
        String startTag = "<return>";
        String endTag = "</return>";
        
        ArrayList<String> messages = new ArrayList<>();
        
        // Si la réponse est vide ou n'est pas au bon format, retourner une liste vide
        if (soapResponse == null || soapResponse.isEmpty() || !soapResponse.contains(startTag)) {
            return messages;
        }
        
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
        
        return messages;
    }
      /**
     * Échappe les caractères XML spéciaux pour éviter les injections XML
     * 
     * @param input Texte à échapper
     * @return Texte échappé sûr pour XML
     */
    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
