# Client SOAP pour Service de Chat Jakarta EE

Ce projet implémente un client SOAP compatible avec un serveur de chat développé avec Jakarta EE.

## Structure du projet

Le projet est organisé comme suit :

```
client/
  ├── build.xml           # Script Ant pour la compilation et l'exécution
  ├── lib/                # Bibliothèques nécessaires
  └── src/                # Code source
      └── com/example/chat/
          ├── JakartaClient.java          # Interface du service de chat
          ├── ChatServiceImpl.java        # Implémentation du client SOAP
          └── JakartaSwingClient.java     # Interface graphique Swing
```

## Fonctionnalités

Le client offre les fonctionnalités suivantes :

- Envoi de messages au serveur de chat
- Réception des messages du chat
- Interface utilisateur graphique intuitive
- Rafraîchissement automatique des messages
- Configuration de l'URL du serveur (permet de se connecter à différents serveurs)
- Gestion des erreurs réseau et des problèmes de connexion

## Technologies utilisées

- Java SE 8+
- Swing pour l'interface graphique
- Requêtes HTTP directes pour la communication SOAP
- ANT comme système de build

## Comment utiliser

### Compilation

Pour compiler le client, utilisez la commande suivante depuis le répertoire `client` :

```bash
ant compile
```

### Exécution

Pour lancer le client graphique, utilisez :

```bash
ant run
```

## Architecture

Le projet utilise une architecture simple basée sur le patron de conception MVC (Modèle-Vue-Contrôleur) :

1. **Modèle** : L'interface `JakartaClient.ChatService` définit le contrat du service
2. **Contrôleur** : `ChatServiceImpl` implémente la communication avec le serveur SOAP
3. **Vue** : `JakartaSwingClient` gère l'interface utilisateur et les interactions

### Communication avec le serveur

La communication avec le serveur utilise des requêtes SOAP envoyées via HTTP. Cela permet d'éviter les problèmes de compatibilité entre les namespaces javax.* et jakarta.* des API JAX-WS.

Les requêtes SOAP sont construites manuellement dans la classe `ChatServiceImpl` et envoyées au serveur via `HttpURLConnection`.

## Configuration

Par défaut, le client se connecte au serveur à l'adresse `http://localhost:8080/chat`. Vous pouvez modifier cette URL dans l'interface graphique pour vous connecter à un autre serveur.

---

© 2025 Client Chat SOAP Jakarta EE
