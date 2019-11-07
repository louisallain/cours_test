#include <stdio.h> // librairie standard C
#include <stdlib.h> // exit
#include <sched.h>  // sche_yield
#include <sys/types.h>
#include <string.h>
#include <sys/sem.h> // semaphore IPC
#include <sys/ipc.h> // services IPC
#include <string.h> // memcpy
#include <unistd.h> // appel systeme fork
#include <fcntl.h>  // appel system unix ES
#include <sys/wait.h> // wait

key_t cle; /* cle ipc */

int semid; /* nom local de l'ensemble des semaphores */
int tab[11]={0};

int tab2fic(char * pathname,int * tab,int size);
int main ( int argc , char **argv ) {
   char * pathname = "./data";
	tab2fic(pathname,tab,10);
  
   ushort init_sem[3]={10,0,1}; 
   
   if ((cle=ftok(pathname,'0')) == -1 ) {
	fprintf(stderr,"Probleme sur ftoks\n");
	exit(1);
   }

   if ((semid=semget(cle,3,IPC_CREAT|IPC_EXCL|0666))==-1) {
	fprintf(stderr,"Probleme sur semget\n");
	exit(2);
   } 

   if (semctl(semid,3,SETALL,init_sem)==-1) {
	fprintf(stderr,"Probleme sur semctl SETALL\n");
	exit(3);
   }
	
	

} 

int tab2fic(char * pathname,int * tab,int size){
  int cible;
  // creation du fichier
   if ( (cible  = open(pathname,O_WRONLY|O_CREAT|O_TRUNC,0666)) < 0){
     fprintf(stderr,"probleme d'ouverture du fichier\n");
     return -1;
   }
 
   if (write(cible,tab,(size+1) * sizeof(int)) !=(size+1) * sizeof(int)) {    
     fprintf(stderr,"probleme d'ecriture du fichier\n");
     return -1;
   }
   close(cible);
   return 0;
}
