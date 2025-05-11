package com.example.chat;

import java.util.List;

/**
 * Définit l'interface du service de chat SOAP.
 * Cette classe contient uniquement l'interface du service pour séparer
 * le contrat de l'implémentation.
 *
 * @author Client Chat SOAP
 * @version 1.0
 */
public class JakartaClient {
    
    /**
     * Interface qui définit les méthodes disponibles du service de chat SOAP.
     * Cette interface est utilisée pour découpler l'utilisation du service
     * de son implémentation.
     */
    public interface ChatService {
        /**
         * Envoie un message au service de chat
         * 
         * @param user Nom d'utilisateur de l'expéditeur
         * @param message Contenu du message à envoyer
         * @throws RuntimeException Si une erreur survient lors de l'envoi
         * @throws IllegalArgumentException Si l'utilisateur ou le message est vide
         */
        void sendMessage(String user, String message);
        
        /**
         * Récupère la liste de tous les messages du chat
         * 
         * @return Liste des messages au format "username: message"
         * @throws RuntimeException Si une erreur survient lors de la récupération
         */
        List<String> getMessages();
    }
}
