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

#include "tab2fic.c"

#define MAX 10 // taille du buffer
#define ARTICLE 0
#define PLACE 1
#define MUTEX 2

int semid; /* nom local de l'ensemble des semaphores */

key_t clef;
int tab[MAX+1] = {0};


// programme principal
// initialise le fichier en faisant un tab2fic
// créer une clé
int main ( int argc , char **argv ) {

	int pid; // numero des fils

	if ((clef=ftok(argv[0],'0')) == -1 ) {
		fprintf(stderr,"Probleme sur ftoks\n");
		exit(1);
   }

	// demande un ensemble de semaphore
	if ((semid=semget(clef, 3, IPC_CREAT|IPC_EXCL|0666)) == -1) {
		fprintf(stderr,"Probleme sur semget\n");
		exit(2);
	} 

	ushort init[3]={0, 10, 1}; //strucutre par initialise le semaphore mutex

	// initialise l'enseble
	if (semctl(semid,3,SETALL,init)==-1) {
			fprintf(stderr,"Probleme sur semctl SETALL\n");
			exit(3);
	}

	char * path = "./marche";

	tab2fic(path, tab, MAX);

	// PRODUCTEUR
	if((pid = fork())) {

		int i = 0;
		while(1) {

			struct sembuf op;

			if(i<50) {

				// P
				op.sem_num=0;op.sem_op=-1;op.sem_flg=0;
				semop(semid,&op,1);
				op.sem_num=2;op.sem_op=-1;op.sem_flg=0;
				semop(semid,&op,1);
				
				// PRODUIT
				fic2tab(path,tab,10);
				tab[0]++;
				tab[tab[0]]=i;
				printf("ajoute : %d\n", i);
				tab2fic(path,tab,10);
				
				// V
				op.sem_num=2;op.sem_op=1;op.sem_flg=0;
				semop(semid,&op,1);
				op.sem_num=1;op.sem_op=1;op.sem_flg=0;
				semop(semid,&op,1);
				i++;
			}
			else 
				break;
		}
		exit(0);
	}

	// CONSOMATEUR
	if((pid = fork() == 0)) {

		while(1) {
			
			struct sembuf op;
			
			// P
			op.sem_num=1;op.sem_op=-1;op.sem_flg=0;
			semop(semid,&op,1);
			op.sem_num=2;op.sem_op=-1;op.sem_flg=0;
			semop(semid,&op,1);

			// CONSOMME
			int cpt;
			fic2tab(path,tab,MAX);
			if(tab[0]>0){
				cpt=tab[1];
				tab[0]--;
				memmove(tab+1,tab+2,tab[0]*sizeof(int));
				printf("retire : %d\n",cpt);
				tab2fic(path,tab,MAX);
			}
			
			// V
			op.sem_num=2;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
			op.sem_num=0;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
			
			if(cpt==49){
				break;
			}

			usleep(200000);
		}
		exit(0);
	}

	wait(NULL);
	wait(NULL);

	semctl(semid,0,IPC_RMID,0);
}