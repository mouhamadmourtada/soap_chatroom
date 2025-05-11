package com.example.chat;

import com.example.chat.client.ChatService;
import com.example.chat.client.ChatServiceImplService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class SwingClient extends JFrame {
    private JTextArea messageArea;
    private JTextField usernameField;
    private JTextField messageField;
    private JButton sendButton;
    private JButton refreshButton;
    private ChatService port;
    
    public SwingClient() {
        // Configuration de la connexion au service SOAP
        ChatServiceImplService service = new ChatServiceImplService();
        port = service.getChatServiceImplPort();
        
        // Configuration de la fenêtre
        setTitle("Chat SOAP Client");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Créer les composants
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
            port.sendMessage(username, message);
            messageField.setText("");
            refreshMessages();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'envoi du message : " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshMessages() {
        try {
            List<String> messages = port.getMessages();
            messageArea.setText("");
            for (String message : messages) {
                messageArea.append(message + "\n");
            }
        } catch (Exception ex) {
            System.err.println("Erreur lors de la récupération des messages : " + ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SwingClient();
            }
        });
    }
}
