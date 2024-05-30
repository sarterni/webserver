# *BUT 1 INFO : SAE 2.04 : Créations et configurations d'un serveur web avec Javanet et XML : Nicolas Sarter*
## Quelle est la différence entre serveur et client ?

En résumé, nous pouvons affirmer qu'un serveur est un ordinateur qui a comme objectif de fournir des données alors qu'un client est un ordinateur (ou un logiciel) qui reçoit les données envoyées par le serveur. Les deux travaillent donc ensemble dans un réseau

## Documentation du projet HttpServer_V1 (Version non configurable)

**Classe : HttpServer_V1**

## Pour lancer le serveur, vous devez compiler et exécuter le programme Java

Etape 1 : compiler le code
avac HttpServer.java 


Etape 2 : éxécuter le code 
java HttpServer

Le port par défaut est 80, vous pouvez spécifiez un port par exemple 
java HttpServer 8080

Etape 3 : allez sur la page du serveur : 
ouvrez un navigateur puis tapez : http://localhost/FichiersSiteWeb/index.html

### Attention à bien indiquer le chemin de votre fichier html principal  à partir de HttpServer.java

## Documentation du projet HttpServer_V2 (Version configurable)

**Classe : HttpServer_V2**

Cette classe implémente un serveur HTTP simple qui écoute les connexions entrantes sur un port spécifié et sert des fichiers statiques à partir d'un répertoire racine configuré.

**Constantes:**

* `DEFAULT_PORT` : Définit le numéro de port par défaut (80) sur lequel écouter si celui-ci n'est pas spécifié dans le fichier de configuration.
* `THREAD_POOL_SIZE` : Définit la taille du pool de threads utilisé pour gérer les requêtes simultanées des clients (10 threads).
* `DEFAULT_ROOT` : Définit le chemin d'accès par défaut au répertoire racine pour servir les fichiers (répertoire actuel).

**Méthode principale (`main`)** :

* Lit le numéro de port et le chemin d'accès au répertoire racine à partir du fichier de configuration `webconfig.xml`.
* Crée un pool de threads avec une taille fixe de `THREAD_POOL_SIZE`.
* Démarre un socket serveur sur le port spécifié.
* Entre dans une boucle infinie pour accepter les connexions entrantes des clients.
* Pour chaque connexion, génère un nouveau thread pour gérer la requête du client en utilisant la méthode `handleClient`.
* Arrête le pool de threads lors de la terminaison du serveur.

**Méthode : `readPortFromXmlConfig(String filePath)`**

* Lit la configuration du numéro de port à partir du fichier XML spécifié.
* Analyse le document XML à l'aide du modèle d'objet document (DOM).
* Extrait la valeur du premier élément nommé `"port"`.
* Renvoie le numéro de port analysé ou le `DEFAULT_PORT` si une erreur se produit pendant l'analyse.

**Méthode : `readRootFromXmlConfig(String filePath)`**

* Lit le chemin d'accès au répertoire racine de la configuration à partir du fichier XML spécifié.
* Analyse le document XML à l'aide du DOM.
* Extrait la valeur du premier élément nommé `"root"`.
* Renvoie le chemin d'accès à la racine extrait ou le `DEFAULT_ROOT` si une erreur se produit pendant l'analyse.

**Méthode : `handleClient(Socket clientSocket, String rootPath)`**

* Gère une requête individuelle d'un client.
* Lit la première ligne de la requête (ligne de requête).
* Valide la ligne de requête :
    * Vérifie si la requête est vide.
    * Vérifie si la méthode de requête est "GET".
    * Vérifie si la requête comprend au moins trois parties séparées par des espaces.
* Extrait le chemin du fichier demandé de la ligne de requête.
* Si le chemin demandé est "/", le définit sur "/index.html" (par défaut).
* Construit le chemin absolu du fichier en combinant le chemin racine et le chemin demandé.
* Vérifie si le fichier demandé existe et est un fichier régulier (pas un répertoire).
* Si le fichier existe :
    * Détermine le type de contenu MIME du fichier.
    * Lit le contenu du fichier dans un tableau d'octets.
    * Envoie une réponse avec le code d'état "200 OK", incluant le type de contenu, la longueur du contenu et le contenu du fichier.
* Si le fichier n'existe pas ou ne peut pas être lu :
    * Envoie une réponse avec le code d'état "404 Not Found".

**Méthode : `sendResponse(OutputStream out, String header)`**

* Envoie un en-tête de réponse au client via le flux de sortie fourni.
* Convertit la chaîne d'en-tête en octets et les écrit dans le flux de sortie.
* Vide le flux de sortie pour s'assurer que les données sont envoyées immédiatement au client.

**Fonctionnalité globale:**

Ce serveur HTTP peut être configuré via un fichier XML (`webconfig.xml`) pour spécifier le numéro de port et le répertoire racine pour servir les fichiers statiques. Il peut gérer les requêtes simultanées des clients à l'aide d'un pool de threads fixe et répond avec le contenu du fichier demandé ou des messages d'erreur appropriés pour les requêtes invalides ou les fichiers inexistants.



