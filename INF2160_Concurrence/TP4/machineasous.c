#include <sys/time.h>
#include <unistd.h> // appel systeme fork
#include <fcntl.h>  // appel system unix ES
#include <stdio.h> // librairie standard C
#include <stdlib.h> // exit
#include <sched.h>  // sche_yield
#include <sys/types.h>
#include <sys/sem.h> // semaphore IPC
#include <sys/ipc.h> // services IPC
#include <sys/wait.h> // wait
#include <string.h>
#include <sys/shm.h>

#define MUTEX_DONNEES_ROULEAU 1

void printUsage() {
	printf("Usage machineasous <nb rouleau>\n");
}

int getRandomInt(int max) {
	
	int init; // valeur pour initialiser le generateur   

	// recupere l'heure systeme pour initialiser le generateur
	time_t t;  //strut
	init= time(&t);

	srandom(init);  // initialisation du generateur
	int ret = random()%max;
	
	return ret;
}

int main ( int argc , char **argv ) {
	
	if(argc != 2) {
		printUsage();
		exit(1);
	}
	
	int nbRouleaux = atoi(argv[1]); // nombre de rouleaux demandés
	printf("Nombre de rouleaux demandés : %d\n", nbRouleaux);
	
	// initialise le segment partagé et le sémaphore
	
	int semid; // nom local de l'ensemble des semaphores
	int shmid; // id du segment partagé
	key_t clef;
	struct sembuf op; // operation sur un semaphore
	
	ushort init_sem[1]={1}; // strucutre pour initialisé le sémaphore mutex
	
	// initialise la clef
	if ((clef=ftok(argv[0], '0')) == -1 ) {
		fprintf(stderr,"Probleme sur ftoks\n");
		exit(1);
	}
	
	// initalise l'id du segment partagé
	if ((shmid=shmget(clef, 4096, IPC_CREAT|0644)) == -1) {  
		fprintf(stderr,"Probleme sur shmget\n");
		exit(2);
	}	 
	
	// initialise l'id de l'ensemble des sémaphores
	if ((semid=semget(clef, 1, IPC_CREAT|0666)) == -1) {
		fprintf(stderr,"Probleme sur semget\n");
		exit(2);
	} 
	
	// initialise l'ensemble
	if (semctl(semid, 1, SETALL, init_sem)==-1) {
		fprintf(stderr,"Probleme sur semctl SETALL\n");
		exit(3);
	}
	
	// Initialise les données des rouleaux
	// Ces données sont sauvegardés sous forme d'un tableau
	// où par exemple : 
	// tableau[2] = 4 <=> le rouleau numéro 2 à la valeur 4
	// Par défaut tous les rouleaux ont la valeur 0
	int * donneesRouleaux;;
	
	/////////////////////////////////////////////////////////:
	
	if ((donneesRouleaux = (int *)shmat(shmid, NULL, 0)) == (int *)-1) {  
				 
		fprintf(stderr,"Probleme sur shmat\n");
		exit(2);
	}
	
	
	
	int pid, indiceRouleauCourant;
	
	for (indiceRouleauCourant = 0; indiceRouleauCourant < nbRouleaux; indiceRouleauCourant++) {
		
		if ((pid = fork())==0) { 
			printf("proc fils debut\n");
			// processus fils
			int valeurRouleauCourant = getRandomInt(10); // on donne à ce rouleau la valeur d'un entier aléatoire [0; 10[

			// sauvegarde ce numero dans le segment partagé
			// P(&MUTEX_DONNEES_ROULEAU)
			op.sem_num = MUTEX_DONNEES_ROULEAU; op.sem_op = -1; op.sem_flg = 0;
			semop(semid, &op, 1);
			
			printf("proc fils fin\n");
			
			// accès à donneesRouleaux
			donneesRouleaux[indiceRouleauCourant] = valeurRouleauCourant;
			printf("%d\n", donneesRouleaux[indiceRouleauCourant]);
			
			printf("proc fils milieu2\n");
			
			shmdt(donneesRouleaux);
			printf("proc fils fin\n");
			// V(&MUTEX_DONNEES_ROULEAU)
			op.sem_num = MUTEX_DONNEES_ROULEAU; op.sem_op = 1; op.sem_flg = 0;
			semop(semid, &op, 1);
			
			
			
			exit(0);  
		}
  } 

  // attend la terminaison des fils
  int i;
  for (i = 0; i < nbRouleaux; i++) {
	  
    int message;
    pid = wait(&message);
  } 
}
