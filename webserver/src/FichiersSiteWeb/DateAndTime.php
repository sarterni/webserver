<!DOCTYPE html>
<html lang="fr">

<head>
    <meta charset="UTF-8">
    <title>Exemple avec la date</title>
</head>

<body>
    <h1>Exemple avec la date</h1>
    <h2>en bash</h2>
    <p>
        <?php
        echo "Nous sommes le " . gmdate("d/m/Y") . " et il est " . gmdate("H:i:s");
        ?>
    </p>
    <h2>en python</h2>
    <p>
        <?php
        $date = shell_exec("python3 -c 'import datetime; print(datetime.datetime.now())'");
        echo "Nous sommes le " . substr($date, 0, 10) . " et il est " . substr($date, 11, 8);
        ?>
    </p>

</body>
</html>




<!-- <!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>

<body>
    <div class=Section1>

        <h1 align=center style='text-align:center'>Date et heure en temps réel</h1>

        <h2>Voici la date et l'heure actuelle:</h2>

        <p class=MsoNormal>Le serveur MiniWeb permet de faire un site web, composé de
            pages au format html. Il ne permet pas de faire s'éxécuter des programmes comme
            des cgi-bin ou des servlets. Il supporte les navigateurs habituels.</p>

        <a href="index.html">Retour à la page d'accueil</a>
    </div>

</body>

</html> -->