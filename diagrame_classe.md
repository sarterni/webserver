## Diagramme de classe serveur http

### Classe HttpServer_V2

Cette classe implémente un serveur HTTP de base qui écoute les connexions entrantes sur un port spécifié, lit les requêtes des clients et envoie des réponses.

### Attributs:

* `DEFAULT_PORT`: Un entier statique final défini sur le port par défaut (80).
* `THREAD_POOL_SIZE`: Un entier statique final définissant la taille du pool de threads utilisé pour gérer les requêtes des clients (10).
* `DEFAULT_ROOT`: Une chaîne statique finale représentant le répertoire racine par défaut pour la diffusion des fichiers ("./").

### Méthodes:

* `main(String[] args)`: Le point d'entrée de l'application. Cette méthode lit le port et le répertoire racine à partir d'un fichier de configuration XML ("webconfig.xml"), crée un pool de threads et démarre le serveur à l'écoute sur le port spécifié. Ensuite, il accepte les connexions entrantes des clients et les traite dans un thread séparé.
* `readPortFromXmlConfig(String filePath)`: Lit le numéro de port à partir du fichier de configuration XML spécifié ("webconfig.xml"). En cas d'erreur de lecture du port, il enregistre le message d'erreur et renvoie le port par défaut (`DEFAULT_PORT`).
* `readRootFromXmlConfig(String filePath)`: Lit le chemin du répertoire racine à partir du fichier de configuration XML spécifié ("webconfig.xml"). En cas d'erreur de lecture du chemin racine, il enregistre le message d'erreur et renvoie le répertoire racine par défaut (`DEFAULT_ROOT`).
* `handleClient(Socket clientSocket, String rootPath)`: Cette méthode gère une connexion cliente entrante. Elle lit la ligne de requête du client, l'enregistre, l'analyse, puis :
    * Vérifie si la requête est une requête GET valide avec trois parties (méthode, chemin et version HTTP). Si ce n'est pas le cas, elle envoie une réponse "400 Bad Request" et ferme la connexion.
    * Extrait le chemin du fichier demandé de la ligne de requête.
    * Si le chemin demandé est "/", elle définit le chemin sur "/index.html" (comportement par défaut pour le répertoire racine).
    * Construit le chemin complet du fichier en combinant le chemin du répertoire racine et le chemin demandé.
    * Vérifie si le fichier demandé existe et s'il s'agit d'un fichier normal (pas d'un répertoire).
    * Si le fichier existe :
        * Détermine le type MIME du fichier à l'aide de `Files.probeContentType`.
        * Lit le contenu du fichier dans un tableau d'octets à l'aide de `Files.readAllBytes`.
        * Envoie une réponse "200 OK" avec les en-têtes appropriés (Content-Type, Content-Length) et le contenu du fichier.
    * Si le fichier n'existe pas ou est un répertoire, elle envoie une réponse "404 Not Found".
    * (Commentaire de réservation : Ajoutez ici plus de logique de gestion des requêtes...)
* `sendResponse(OutputStream out, String response)`: Envoie la chaîne de réponse spécifiée au client via le flux de sortie fourni.
