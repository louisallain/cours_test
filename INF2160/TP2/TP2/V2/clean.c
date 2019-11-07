#include <stdio.h> // librairie standard C
#include <stdlib.h> // exit
#include <sched.h>  // sche_yield
#include <sys/types.h>
#include <string.h>
#include <sys/sem.h> // semaphore IPC
#include <sys/ipc.h> // services IPC

key_t cle; /* cle ipc */

int semid; /* nom local de l'ensemble des semaphores */

int main ( int argc , char **argv ) {
   char * pathname = "./data";
   
   if ((cle=ftok(pathname,'0')) == -1 ) {
	fprintf(stderr,"Probleme sur ftoks\n");
	exit(1);
   }
   
   if ((semid=semget(cle,3,0))==-1) {
	fprintf(stderr,"Probleme sur semget\n");
	exit(2);
   } 

   semctl(semid,0,IPC_RMID,0);
} 

