#include <unistd.h> // appel systeme fork
#include <fcntl.h>  // appel system unix ES
#include <stdio.h> // librairie standard C
#include <stdlib.h> // exit
#include <sched.h>  // sche_yield
#include <sys/types.h>
#include <string.h>
#include <sys/sem.h> // semaphore IPC
#include <sys/ipc.h> // services IPC
#include <sys/wait.h> // wait

key_t cle; /* cle ipc */

int semid; /* nom local de l'ensemble des semaphores */
int tab[11]={0};

//Fichier et tableau buffer


int fic2tab(char * pathname,int * tab,int size);
int tab2fic(char * pathname,int * tab,int size);

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
   
	int i=0;
	while(1){
		struct sembuf op;
		
		if(i<50){
			op.sem_num=0;op.sem_op=-1;op.sem_flg=0;
			semop(semid,&op,1);
			op.sem_num=2;op.sem_op=-1;op.sem_flg=0;
			semop(semid,&op,1);
			
			fic2tab(pathname,tab,10);
			tab[0]++;
			tab[tab[0]]=i;
			printf("Le producteur ajoute %d\n",i);
			tab2fic(pathname,tab,10);
			
			op.sem_num=2;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
			op.sem_num=1;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
			i++;
		}
		else break;
	}
	printf("Le producteur a fini\n");

} 

int fic2tab(char * pathname,int * tab,int size){
  int cible;
   // lecture  du fichier
   if ( (cible  = open(pathname,O_RDONLY)) < 0){
     fprintf(stderr,"probleme d'ouverture du fichier\n");
     return -1;
   }
 
   if (read(cible,tab,(size+1) * sizeof(int)) !=(size+1) * sizeof(int)) {    
     fprintf(stderr,"probleme de lecture du fichier\n");
     return -1;
   }
   close(cible);
   return 0;
}


// tab2 fic
// ecrit un tableau dans un fichier 
// size :  la taille réelle du tableau (tab[0] non compris)
//

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