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

/*
 * Lecteur.
 */
int main ( int argc , char **argv ) {
	
	
	int semid; // nom local de l'ensemble des semaphores
	int shmid; // id du segment partagé
	key_t clef;
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
	
	// P(&SEM_NBL)
	op.sem_num = SEM_NBL; op.sem_op = -1; op.sem_flg = 0;
	semop(semid, &op, 1);

	// accès à nbL
	int * nbL;
	if (*(nbL=(int *)shmat(shmid,NULL,SHM_RDONLY))==-1) {     
		fprintf(stderr,"Probleme sur shmat\n");
		exit(2);
	}
	*nbL++;

	// Lecture
	if(*nbL == 1) {
		// P(&INFO)
		op.sem_num = SEM_M_INFO; op.sem_op = -1; op.sem_flg = 0;
		semop(semid, &op, 1);
	}
	
	// V(&SEM_NBL)
	op.sem_num = SEM_NBL; op.sem_op = 1; op.sem_flg = 0;
	semop(semid, &op, 1);

	// accès à l'info
	FILE * fic;
	if((fic = fopen("/tmp/mdj", "r")) != NULL) {
		char str[4096];
		while(fgets(str, 4096, fic) != (char *) NULL) {
			printf("%d %s", *nbL, str);
			sleep(5);
		}
	}
	fclose(fic);
	
	// P(&SEM_NBL)
	op.sem_num = SEM_NBL; op.sem_op = -1; op.sem_flg = 0;
	semop(semid, &op, 1);
	
	if (*(nbL=(int *)shmat(shmid,NULL,SHM_RDONLY))==-1) {     
		fprintf(stderr,"Probleme sur shmat\n");
		exit(2);
	}
	*nbL--;

	if(*nbL == 0) {
		// V(&INFO)
		op.sem_num = SEM_M_INFO; op.sem_op = 1; op.sem_flg = 0;
		semop(semid, &op, 1);
	}
	
	// V(&SEM_NBL)
	op.sem_num = SEM_NBL; op.sem_op = 1; op.sem_flg = 0;
	semop(semid, &op, 1);
	
}
