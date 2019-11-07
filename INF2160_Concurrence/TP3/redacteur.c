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

int shmid; // id du segment partagé
int semid; // nom local de l'ensemble des semaphores
key_t clef;

/*
 * Rédacteur.
 */
int main ( int argc , char **argv ) {
	
	struct sembuf op; // operation sur un semaphore

	if ((clef=ftok("/tmp/mdj",'0')) == -1 ) {
		fprintf(stderr,"Probleme sur ftoks\n");
		exit(1);
	}
	
	if ((shmid=shmget(clef,4096,IPC_CREAT|0644))==-1) {  
		fprintf(stderr,"Probleme sur shmget\n");
		exit(2);
	}

	// initialise l'id de l'ensemble des sémaphores
	if ((semid=semget(clef, 2, IPC_CREAT|0666)) == -1) {
		fprintf(stderr,"Probleme sur semget\n");
		exit(2);
	} 
	
	// P(&INFO)
	op.sem_num = SEM_M_INFO; op.sem_op = -1; op.sem_flg = 0;
	semop(semid, &op, 1);
	
	// Rédaction
	system("cat > /tmp/mdj");
	
	// V(&INFO)
	op.sem_num = SEM_M_INFO; op.sem_op = 1; op.sem_flg = 0;
	semop(semid, &op, 1);
}

