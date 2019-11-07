#include <unistd.h> // appel systeme fork
#include <fcntl.h>  // appel system unix ES
#include <stdio.h> // librairie standard C
#include <stdlib.h> // exit
#include <sched.h>  // sche_yield
#include <sys/types.h>
#include <sys/sem.h> // semaphore IPC
#include <sys/ipc.h> // services IPC
#include <sys/wait.h> // wait
#include <string.h> // memcpy


key_t cle;
int semid;
int tab[11]={0};

int fic2tab(char * pathname,int * tab,int size);
int tab2fic(char * pathname,int * tab,int size);
int main (int argc,char **argv) {

	int pid;
	
	if ((cle=ftok(argv[0],'0')) == -1 ) {
		fprintf(stderr,"Probleme sur ftoks\n");
		exit(1);
   }
   

   
   if((semid=semget(cle,3,IPC_CREAT|IPC_EXCL|0666))==-1){
	   	fprintf(stderr,"Probleme sur semget\n");
		exit(1);
   }
 
	//int semid = semget(cle,3,IPC_CREAT|IPC_EXCL|0666);
	ushort init_sem[3]={10,0,1}; //place article mutex
	
	// initialise l'enseble
   if (semctl(semid,3,SETALL,init_sem)==-1) {
		fprintf(stderr,"Probleme sur semctl SETALL\n");
		exit(3);
   }
   
   
   //Fichier et tableau buffer
   char * pathname = "./data";

   tab2fic(pathname,tab,10);
   
	if ((pid = fork()) == 0) {
         /* processus fils producteur*/
         int i =0;
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
			printf("prod add : %d\n",i);
			tab2fic(pathname,tab,10);
			
			op.sem_num=2;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
			op.sem_num=1;op.sem_op=1;op.sem_flg=0;
			semop(semid,&op,1);
			i++;
		}
		else break;
	}
		exit(0);  
	}

	if ((pid = fork()) == 0) {
		// Processus fils consommateur
	while(1){
		usleep(200000);
		struct sembuf op;
		
		op.sem_num=1;op.sem_op=-1;op.sem_flg=0;
		semop(semid,&op,1);
		op.sem_num=2;op.sem_op=-1;op.sem_flg=0;
		semop(semid,&op,1);
		int cons;
		
		fic2tab(pathname,tab,10);
		if(tab[0]>0){
			cons=tab[1];
			tab[0]--;
			memmove(tab+1,tab+2,tab[0]*sizeof(int));
			printf("Cons : %d\n",cons);
			tab2fic(pathname,tab,10);
		}
		
		op.sem_num=2;op.sem_op=1;op.sem_flg=0;
		semop(semid,&op,1);
		op.sem_num=0;op.sem_op=1;op.sem_flg=0;
		semop(semid,&op,1);
		
		if(cons==49){
			break;
		}
	}
		 exit(0);  
	}
 
	      /* attende des fils */
	      
      int message;
      pid =wait(&message);
      pid =wait(&message);
     semctl(semid,0,IPC_RMID,0);
}

// fic2tab
// charge un tableau à partir d'un fichier 
// size :  la taille réelle  du tableau (tab[0] non compris)
//
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
