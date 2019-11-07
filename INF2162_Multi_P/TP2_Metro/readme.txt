. Pour gérer le graphe du réseau de la classe Reseau, j'utilise une libraire qui s'appelle GraphStream. "GraphStream est une librairie Java pour la modélisation et l'analyse de graphes.", voir leur site web http://graphstream-project.org/

. Procédure pour générer à nouveau le jar exécutable de mon TP2 :
	1. En se positionnant dans le répertoire racine (où il y a le readme.txt), compiler les sources : javac -cp ".;./lib/*" ./src/metro/*.java -d .
	2. Créer l'exécutable : jar cvfm metro.jar MANIFEST.MF metro lib res

. Pour lancer le résultat de mon TP2 :
	1. Ouvrir un terminal et taper java -jar Allain_Louis_Metro.jar

. Les sorties sur la console résultent des tests qui sont effectués dans le programme principal de mon TP.


