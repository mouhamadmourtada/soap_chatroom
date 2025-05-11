package com.example.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Client SOAP graphique qui communique avec le serveur ChatService à l'aide
 * de requêtes HTTP directes (sans dépendances à des bibliothèques JAX-WS).
 */
public class SimpleSwingClient extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String ENDPOINT_URL = "http://localhost:8080/chat";
    private static final String SOAP_ACTION = "";
    
    private JTextArea messageArea;
    private JTextField usernameField;
    private JTextField messageField;
    private JButton sendButton;
    private JButton refreshButton;
    
    public SimpleSwingClient() {
        // Configuration de la fenêtre
        setTitle("Chat SOAP Client");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Création des composants
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        
        usernameField = new JTextField(15);
        messageField = new JTextField(30);
        sendButton = new JButton("Envoyer");
        refreshButton = new JButton("Rafraîchir");
        
        // Panneau pour les contrôles
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(new JLabel("Nom :"));
        controlPanel.add(usernameField);
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout());
        messagePanel.add(new JLabel("Message :"));
        messagePanel.add(messageField);
        messagePanel.add(sendButton);
        messagePanel.add(refreshButton);
        
        // Organisation des panneaux
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(messagePanel, BorderLayout.SOUTH);
        
        // Ajouter le panneau principal à la fenêtre
        setContentPane(mainPanel);
        
        // Gestionnaires d'événements
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshMessages();
            }
        });
        
        // Timer pour rafraîchir les messages automatiquement
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshMessages();
            }
        });
        timer.start();
        
        // Afficher la fenêtre
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Charger les messages au démarrage
        refreshMessages();
    }
    
    private void sendMessage() {
        String username = usernameField.getText().trim();
        String message = messageField.getText().trim();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un nom d'utilisateur", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un message", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:chat=\"http://chat.example.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <chat:sendMessage>\n" +
                "         <arg0>" + escapeXml(username) + "</arg0>\n" +
                "         <arg1>" + escapeXml(message) + "</arg1>\n" +
                "      </chat:sendMessage>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
            
            sendSoapRequest(soapRequest);
            messageField.setText("");
            refreshMessages();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'envoi du message : " + ex.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshMessages() {
        try {
            String soapRequest = 
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:chat=\"http://chat.example.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <chat:getMessages/>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
            
            String response = sendSoapRequest(soapRequest);
            String[] messages = parseMessages(response);
            
            messageArea.setText("");
            for (String message : messages) {
                messageArea.append(message + "\n");
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la récupération des messages : " + ex.getMessage());
        }
    }
    
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
    
    private static String[] parseMessages(String soapResponse) {
        // Méthode simple pour extraire les messages de la réponse SOAP
        String startTag = "<return>";
        String endTag = "</return>";
        
        ArrayList<String> messages = new ArrayList<>();
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
    
    private static String escapeXml(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimpleSwingClient();
            }
        });
    }
}
