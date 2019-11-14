#include <time.h>
#include <unistd.h> 
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <string.h>

void getDate(char* buffDate) {
	
	time_t tps;
	tps = time(0);
	strcpy(buffDate, ctime(&tps));
}

int main ( int argc , char **argv ) {
	
	////////////////////////
 	// CREATION DES TUBES //
	////////////////////////
	
	mode_t mode=S_IRUSR|S_IWUSR|S_IRGRP;
	
	// tubeDemandes
	if (mkfifo("/tmp/tubeDemandes", mode) == -1) {
		fprintf(stderr,"Tube deja cree : '/tmp/tubeDemandes'\n");
		// exit(1);
	} else printf("Creation du tube nomme : '/tmp/tubeDemandes' \n");  
	
	
	
	////////////////////////
 	// LECTURE DES TUBES  //
	////////////////////////
	
	int tDem, t, pid; 
	
	// OUVERTURE
	if ((tDem = open("/tmp/tubeDemandes", O_RDONLY))< 0) {
		fprintf(stderr,"Probleme d'ouverture du tube '/tmp/tubeDemandes'\n");
		exit(1);
	}    
	printf("conso '/tmp/tubeDemandes'\n");
	
	// LECTURE
	// un client envoie le nom d'un tube nommé (son pid)
	// le serveur envoie la date sur ce tube nommé
	int i = 0;
	int nbFils = 0;
	int receivedPid;
	
	while(1) {
		while((read(tDem, &receivedPid, sizeof(receivedPid)) == sizeof(receivedPid))) {
					
			if((pid = fork()) == 0) {
				
				char nomTubePid[32] = { };
				sprintf(nomTubePid, "/tmp/t%d", receivedPid);
				
				if (mkfifo(nomTubePid, mode) == -1) {
					fprintf(stderr,"Tube deja cree : '%s' \n", nomTubePid);
					// exit(1);
				} else printf("Creation du tube nomme : '%s' \n", nomTubePid);  
				
				int tRec;
				if((tRec = open(nomTubePid, O_WRONLY)) < 0) {
					fprintf(stderr,"Probleme d'ouverture du tube '%s'\n", nomTubePid);
					exit(1);
				}
				printf("conso : '%s' \n", nomTubePid);
				char date[128];
				getDate(date);
				printf("%s \n", date);
				write(tRec, &date, 128);
				close(tRec);
				
				nbFils++;
			}
			
			i++;
		}
	}
	close(tDem);
	for (i = 0; i < nbFils; i++) {
    
		int message;
		pid = wait(&message);
	} 
}
