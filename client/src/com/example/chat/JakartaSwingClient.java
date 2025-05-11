package com.example.chat;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Client graphique moderne pour le service de chat SOAP.
 * Cette interface utilise Swing avec un design moderne pour offrir 
 * une exp�rience utilisateur agr�able et communique avec le service SOAP.
 *
 * @author Client Chat SOAP
 * @version 2.0
 */
public class JakartaSwingClient extends JFrame {
    private static final long serialVersionUID = 1L;
    
    /** Service de chat SOAP */
    private JakartaClient.ChatService chatService;
    
    /** Zone d''affichage des messages */
    private JTextArea messageArea;
    
    /** Champ pour saisir un nouveau message */
    private JTextField messageField;
    
    /** Nom d''utilisateur enregistr� */
    private String username;
    
    /** Panneau principal */
    private JPanel mainPanel;
    
    /** URL du serveur par d�faut */
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080/chat";
    
    /** Couleurs du th�me */
    private static final Color PRIMARY_COLOR = new Color(64, 81, 181);   // Bleu indigo
    private static final Color ACCENT_COLOR = new Color(255, 64, 129);   // Rose
    private static final Color BG_COLOR = new Color(250, 250, 250);      // Blanc cass�
    
    /**
     * Constructeur qui initialise le client de chat
     */
    public JakartaSwingClient() {
        try {
            // Initialisation du service SOAP
            ChatServiceImpl serviceImpl = new ChatServiceImpl();
            serviceImpl.setEndpointUrl(DEFAULT_SERVER_URL);
            this.chatService = serviceImpl;
            
            // Demander le pseudonyme au lancement
            requestUsername();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur d''initialisation : " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Demande le pseudonyme � l''utilisateur via une bo�te de dialogue
     */
    private void requestUsername() {
        // Cr�ation du panneau personnalis�
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Titre avec une police plus grande
        JLabel titleLabel = new JLabel("Bienvenue dans le Chat SOAP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Image de logo (ic�ne par d�faut si pas d''image)
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(UIManager.getIcon("OptionPane.questionIcon"));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Message explicatif
        JLabel messageLabel = new JLabel("Veuillez entrer votre pseudonyme pour rejoindre le chat");
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Champ de texte pour le pseudonyme
        JTextField usernameField = new JTextField(15);
        usernameField.setMaximumSize(new Dimension(300, 30));
        
        // Ajout des composants
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(usernameField);
        
        // Affichage de la bo�te de dialogue
        int result = JOptionPane.showConfirmDialog(null, panel, 
                "Connexion au Chat", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        // Traitement du r�sultat
        if (result == JOptionPane.OK_OPTION) {
            String inputUsername = usernameField.getText().trim();
            if (inputUsername.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Le pseudonyme ne peut pas �tre vide.", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                requestUsername();  // Demander � nouveau le pseudonyme
            } else {
                username = inputUsername;
                initializeUI();  // Initialiser l''interface principale
            }
        } else {
            System.exit(0);  // Fermeture si annulation
        }
    }
    
    /**
     * Initialise l''interface utilisateur principale
     */
    private void initializeUI() {
        // Configuration de la fen�tre
        setTitle("Chat SOAP - " + username);
        setSize(800, 600);
        setMinimumSize(new Dimension(500, 400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Panneau principal avec un fond blanc
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setBackground(BG_COLOR);
        
        // Cr�ation de la barre d''en-t�te
        JPanel headerPanel = createHeaderPanel();
        
        // Zone de messages avec style
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setMargin(new Insets(10, 10, 10, 10));
        messageArea.setBackground(BG_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Panneau de saisie de message
        JPanel inputPanel = createInputPanel();
        
        // Assemblage des �l�ments
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Param�tres finaux de la fen�tre
        setContentPane(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Focus sur le champ de message
        messageField.requestFocusInWindow();
        
        // Rafra�chir les messages imm�diatement puis programmer les rafra�chissements
        refreshMessages();
        startMessageRefresher();
    }
    
    /**
     * Cr�e le panneau d''en-t�te avec le titre et les informations utilisateur
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        
        JLabel titleLabel = new JLabel("Chat SOAP");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel usernameLabel = new JLabel("Connect� en tant que: " + username);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameLabel.setForeground(Color.WHITE);
        
        // Bouton pour les param�tres (optionel)
        JButton settingsButton = new JButton("");
        settingsButton.setFocusPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setFont(new Font("Arial", Font.PLAIN, 18));
        settingsButton.addActionListener(e -> showSettings());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(usernameLabel, BorderLayout.CENTER);
        headerPanel.add(settingsButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    /**
     * Cr�e le panneau de saisie de message en bas
     */
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        inputPanel.setBackground(BG_COLOR);
        
        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setMargin(new Insets(5, 10, 5, 10));
        
        // Style moderne pour le champ de texte
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JButton sendButton = new JButton("Envoyer");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(ACCENT_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());
        
        // Envoyer le message avec la touche Entr�e
        messageField.addActionListener(e -> sendMessage());
        
        // Ajouter une marge entre les composants
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.add(sendButton, BorderLayout.CENTER);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    /**
     * Affiche une bo�te de dialogue pour les param�tres
     */
    private void showSettings() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel serverPanel = new JPanel(new BorderLayout(10, 0));
        JLabel urlLabel = new JLabel("URL du serveur:");
        JTextField urlField = new JTextField(DEFAULT_SERVER_URL);
        
        serverPanel.add(urlLabel, BorderLayout.WEST);
        serverPanel.add(urlField, BorderLayout.CENTER);
        
        panel.add(new JLabel("Param�tres du chat"), BorderLayout.NORTH);
        panel.add(serverPanel, BorderLayout.CENTER);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Param�tres", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String serverUrl = urlField.getText().trim();
            if (!serverUrl.isEmpty()) {
                try {
                    // Cr�er une nouvelle impl�mentation avec la nouvelle URL
                    ChatServiceImpl serviceImpl = new ChatServiceImpl();
                    serviceImpl.setEndpointUrl(serverUrl);
                    this.chatService = serviceImpl;
                    
                    JOptionPane.showMessageDialog(this, 
                            "URL du serveur modifi�e: " + serverUrl,
                            "Succ�s", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Tester la connexion en rafra�chissant
                    refreshMessages();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                            "Erreur lors de la modification de l''URL: " + e.getMessage(),
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * D�marre le rafra�chissement automatique des messages
     */
    private void startMessageRefresher() {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> refreshMessages());
            }
        }, 0, 3000);  // Rafra�chir toutes les 3 secondes
    }
    
    /**
     * Envoie un message au serveur
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            return;  // Ignorer les messages vides
        }
        
        try {
            chatService.sendMessage(username, message);
            messageField.setText("");  // Vider le champ apr�s envoi
            refreshMessages();  // Rafra�chir imm�diatement
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                    "Erreur lors de l''envoi du message: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Rafra�chit l''affichage des messages
     */
    private void refreshMessages() {
        try {
            List<String> messages = chatService.getMessages();
            
            // Garder la position du d�filement
            JScrollBar scrollBar = ((JScrollPane) messageArea.getParent().getParent()).getVerticalScrollBar();
            boolean shouldScroll = scrollBar.getValue() + scrollBar.getHeight() >= scrollBar.getMaximum() - 20;
            
            StringBuilder formattedMessages = new StringBuilder();
            
            if (messages.isEmpty()) {
                formattedMessages.append("Aucun message dans le chat. Soyez le premier � �crire !\n");
            } else {
                for (String msg : messages) {
                    // Mise en forme des messages
                    if (msg.startsWith(username + ": ")) {
                        // Mes messages (align�s � droite)
                        formattedMessages.append("\n[ Moi ] ");
                        formattedMessages.append(msg.substring((username + ": ").length()));
                        formattedMessages.append("\n");
                    } else {
                        // Messages des autres
                        formattedMessages.append("\n");
                        formattedMessages.append(msg);
                        formattedMessages.append("\n");
                    }
                }
            }
            
            messageArea.setText(formattedMessages.toString());
            
            // D�filer vers le bas si n�cessaire
            if (shouldScroll) {
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = ((JScrollPane) messageArea.getParent().getParent()).getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            }
            
        } catch (Exception ex) {
            // G�rer silencieusement les erreurs de rafra�chissement automatique
            System.err.println("Erreur de rafra�chissement: " + ex.getMessage());
        }
    }
    
    /**
     * Point d''entr�e du programme
     */
    public static void main(String[] args) {
        // Utiliser le look and feel moderne Nimbus si disponible
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                // Fallback vers le look and feel du syst�me
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignorer
            }
        }
        
        SwingUtilities.invokeLater(() -> new JakartaSwingClient());
    }
}
