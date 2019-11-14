#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sched.h>
#include <time.h>
#include <sys/resource.h>
#include <sys/time.h>   // for gettimeofday()
#include <sys/ipc.h>
#include <sys/shm.h>
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
#include <time.h> 

#define PLACE 10
#define ARTICLE 0
#define MUTEX 1

typedef struct {
	char* ins;
} t_instruction ;

int main (int argc,char **argv) {
	
	/*
	 * Compter le nombre d'instructions en cours ne fonctionne pas de cette manière mais 
	 * je n'ai pas eu le temps de corriger çà
	 */
	int NBINS = 0;
	
	if(argc != 2) {
		printf("Usage : maitre N < file\n");
		exit(1);
	}
	
	int nbEsclaves = atoi(argv[1]);
	printf("Nb esclaves : %d\n", nbEsclaves);
	
	char c;
	int pid, i, nbRep, delai;
	
	/*
	 * "Programme principal" maître.
	 */	
	  
	// Initialisation
	key_t clef;
	int semid; // nom local de l'ensemble des semaphores
	int shmid; // id du segment partagé
	struct sembuf op; // operation sur un semaphore
	
	ushort init_sem[3]={10, 0, 1}; // strucutre pour initialisé le sémaphore mutex
	
	// initialise la clef
	if ((clef=ftok(argv[0], '4')) == -1 ) {
		fprintf(stderr,"Probleme sur ftoks\n");
		exit(1);
	}
	
	// initialise l'id de l'ensemble des sémaphores
	if ((semid=semget(clef,3,IPC_CREAT|0666))==-1) {
		fprintf(stderr,"Probleme sur semget\n");
		exit(2);
	} 
	
	// initialise l'ensemble
	if (semctl(semid, 3, SETALL, init_sem)==-1) {
		fprintf(stderr,"Probleme sur semctl SETALL\n");
		exit(3);
	}
	
	// initalise l'id du segment partagé
	if ((shmid=shmget(clef, 4096, IPC_CREAT|0644)) == -1) {  
		fprintf(stderr,"Probleme sur shmget\n");
		exit(2);
	}	 
	
	t_instruction * instructions;
		
	if ((instructions = (t_instruction *)shmat(shmid, 0, 0)) == (t_instruction *)-1) {  
				 
		fprintf(stderr,"Probleme sur shmat\n");
		exit(2);
	}
	//instructions[0].nbIns = 0;
	 
	// Produit des instructions dans la mémoire partagée
	if((pid = fork()) == 0) {
	
		while(scanf(" %c%u%u", &c, &nbRep, &delai) == 3) {
			
			op.sem_num=0;op.sem_op=-1;op.sem_flg=0;
			semop(semid,&op,1);
			op.sem_num=2;op.sem_op=-1;op.sem_flg=0;
			semop(semid,&op,1);
			
			char instru[128];
			sprintf(instru, "%c%d%d", c, nbRep, delai);
			instructions[NBINS].ins = instru;
			printf("Maître ajoute instruction n : %d : %s\n", NBINS, instructions[NBINS].ins);
			NBINS++;
			
			op.sem_num=2;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
			op.sem_num=1;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
		}
		printf("Maitre n'a plus d'instructions a donner\n");
		
		exit(0);
	}
	//~ usleep(200000);
	
	/*
	 * Processus esclaves.
	 */
	for(i = 0; i++; i < nbEsclaves) {
		
		// Consomme
		if((pid = fork()) == 0) {
			
			while(1) {
				
				op.sem_num=1;op.sem_op=-1;op.sem_flg=0;
				semop(semid,&op,1);
				op.sem_num=2;op.sem_op=-1;op.sem_flg=0;
				semop(semid,&op,1);
				
				if(NBINS > 0) {
					printf("Conso instructions n : %d : %s\n", NBINS, instructions[NBINS].ins); 
					
					NBINS--;
				}
				else {
					printf("Aucune instruction a consommer\n");
				}
				
				op.sem_num=2;op.sem_op=1;op.sem_flg=0;
				semop(semid,&op,1);
				op.sem_num=0;op.sem_op=1;op.sem_flg=0;
				semop(semid,&op,1);
			}
			exit(0); 
		}
	}
	
	
	// attend ...
	wait(NULL);
	for (i = 0; i < nbEsclaves; i++) {
	  
		wait(NULL);
	} 
	shmdt(instructions);
}
