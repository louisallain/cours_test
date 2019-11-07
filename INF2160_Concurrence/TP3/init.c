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

// sémaphores
#define SEM_M_INFO 1 // accès à l'info (mutex)
#define SEM_NBL 2 // accès à la variable comptant le nombre de lecteurs

// variable partagée
int nbL = 0; // compte le nombre de lecteur

/*
 * Initialise les sémaphore et créer un fichier vide.
 */
int main ( int argc , char **argv ) {
	
	int semid; // nom local de l'ensemble des semaphores
	int shmid; // id du segment partagé
	key_t clef;
	
	// créer le fichier
	system("touch /tmp/mdj");
	
	// initialise la clef
	if ((clef=ftok("/tmp/mdj",'0')) == -1 ) {
		fprintf(stderr,"Probleme sur ftoks\n");
		exit(1);
	}
	
	// initalise l'id du segment partagé
	if ((shmid=shmget(clef,4096,IPC_CREAT|0644))==-1) {  
		fprintf(stderr,"Probleme sur shmget\n");
		exit(2);
	}	 
	
	// initialise l'id de l'ensemble des sémaphores
	if ((semid=semget(clef, 2, IPC_CREAT|0666)) == -1) {
		fprintf(stderr,"Probleme sur semget\n");
		exit(2);
	} 
	
	ushort init[2]={1, 1};
	
	// initialise l'ensemble
	if (semctl(semid,2,SETALL,init)==-1) {
			fprintf(stderr,"Probleme sur semctl SETALL\n");
			exit(3);
	}
	
	int * nbL;
	if (*(nbL=(int *)shmat(shmid,NULL,0))==-1) {     
		fprintf(stderr,"Probleme sur shmat\n");
		exit(2);
	}
	*nbL = 0;
	shmdt(nbL);
}
