# Exécutable permettant de fabriquer la version de production de l'application et de la déployer à l'adresse cyberkey.surge.sh

npm run build &&
echo '*' > build/CORS &&
surge build cyberkey.surge.sh
