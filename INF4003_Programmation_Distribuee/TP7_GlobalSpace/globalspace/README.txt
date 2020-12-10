- Les sources se trouvent dans "dtpsace".
- L'exéctable est "dtspace.jar"
- Déployer l'application : exéctuer le script "make.sh"
- Exécuter les tests : exéctuer le script "run.sh"

- Les tests couvrent les méthodes de la classes GlobalSpace en lançant un producteur qui écrit dans sa
mémoire de tuples un tuple. Ensuite, trois autres consommateur vont selon le paramètre soit lire et prendre un
tuple de manière non bloquante soit lire et prendre un tuple de manière bloquante.
La manière de faire est décrit dans la class TupleMemoryApplicationMaster.

- Ajout d'une classe permettant de configurer le manager de sécurité à l'exécution "MinimalPolicy".

- Le programme fonctionne correctement, les résultats attendus sont les bons, excepté qu'à l'exécution,
yarn produit sur la sortie une erreur de mémoire virtuelle. Pourtant les logs affichent le bon résultat
lors de l'écriture et de la lecture avec les méthodes de la classe GlobalSpace.
