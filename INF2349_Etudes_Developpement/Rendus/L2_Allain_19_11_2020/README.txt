# Pour installer l'application :
	- Mettre le fichier cyberkey.apk sur un téléphone Android puis l'installer.

# Utilisation de l'application :
	
	- Page de connexion :
		- Si vous posséder un compte il suffit de s'y connecter avec l'adresse email (de l'UBS)
		  avec laquelle vous avez créé votre compte.
		- Sinon, il faut utiliser le bouton "CREER SON COMPTE ?".

	- Page de création de compte :
		- A ce moment il faut renseigner les informations demandées.
			- Email : adresse email du domaine de l'UBS.
			- Mots de passe : chaine de caractère d'au moins 8 lettres.
		- Si toutes les informations sont valides, le bouton "CREER" permet de créer son compte
		  et de se connecter à l'application par la même occasion.

	- Page d'accueil : calendrier des créneaux (icône calendrier)
		
		- Sur cette partie de la page d'accueil, tous les créneaux actuellement définis par
		  l'administrateur sont affichés sous forme d'une liste de créneaux séparés par semaine 
		  et rangés par ordre décroissant en fonction de la date du début du créneau.
		- Chaque créneau possède un bouton permettant de demander l'accès à celui-ci. Si l'accès
		  a déjà été demandé pour un créneau, le bouton est désactivé et l'état de la demande
		  est affiché dans ce bouton.
	
	- Page d'accueil : déverrouillage de la porte (icône clef)

		- Sur cette partie de la page d'accueil, un bouton central permet de déverrouiller la 
		  serrure de la porte. Pour le moment, le système de serrure n'a pas encore été
		  développé, donc ce bouton va, pour le moment, scanner les périphériques BLE alentours.
		  Afin de se connecter à l'ESP32 de la serure. Puisqu'il n'existe pas, la recherche va
		  échouer. Le protocole de déverrouillage est quant à lui déjà implémenté et il suffira
		  de développer le système de la serrure pour effectivement déverrouiller la porte.
			
		- Le protocole est le suivant :	
		  Vérifie les accès de l'utilisateur au créneau en cours. Si l'utilisateur y a accès 
		  ou s'il est VIP alors démarre la procédure d'authentification aurpès du système de la
		  serrure qui est :
		  Scan les périphériques BLE alentours à la recherche de l'adresse MAC de l'ESP32 de la 
		  serrure. Si la recherche échoue, informe l'utilisateur. Si la recherche aboutie alors
		  se connecte à l'ESP32. Si la connexion échoue, informe l'utilisateur. Si la connexion
		  aboutie alors envoi écrit sur la caractéristique BLE préalablement définie les infos 
		  nécessaires à l'authentification (l'id de l'utilisateur dans la base de données).
		  Ensuite, lit sur la même caractéristique BLE une chaine de caractère "challenge" que
		  doit envoyé l'ESP32. Signe cette chaine "challenge" avec la clef privée de
		  l'utilisateur créée à la création du compte et renouvelée lors de chaque connexion.
		  La signature de cette chaine "challenge" est ensuite écrite sur la caractéristique BLE.
		  Lit la caractéristique BLE afin de savoir si l'on est authentifié ou non et donc si
		  la porte est bien ouverte ou pas.
		
		- Problème connu : puisque l'application n'a pas été installer depuis le Play Store de
		  Google, par défaut le téléphone va refuser la permission d'accéder à la localisation
		  de l'appareil (nécessaire au bon fonctionnement de BLE). Pour le moment, puisqu'il
		  s'agit d'une version "prototype". Il faut manuellement aller dans les paramètres des
		  autorisations de l'application et toujours autoriser les informations de localisation
		  pour l'application.

	- Page d'accueil : paramètres (icône engrenage)
	
		- Sur cette partie de la page d'accueil, un bouton permet de demander l'accès VIP
		  (sans contraintes de créneaux), l'état de la demande est affiché dans le bouton.
		  Un autre bouton permet de supprimer son compte. Si la dernière connexion de l'utilisateur
		  est trop vieille alors il faudra d'abord se déconnecter puis se reconnecter.