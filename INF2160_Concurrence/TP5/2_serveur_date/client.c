#include <time.h>
#include <unistd.h> 
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/wait.h>

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
	
	int tDem;

	// OUVERTURE
	if ((tDem = open("/tmp/tubeDemandes", O_WRONLY)) < 0) {
		fprintf(stderr,"Probleme d'ouverture du tube '/tmp/tubeDemandes'\n");
		exit(1);
	}    

	// ECRITURE
	int currentPid = getpid();
	write(tDem, &currentPid, sizeof(currentPid));
	close(tDem);
	
	
	// LECTURE DE LA DATE RECUE
	char nomTubePid[32] = { };
	sprintf(nomTubePid, "/tmp/t%d", currentPid);
	
	
	if (mkfifo(nomTubePid, mode) == -1) {
		fprintf(stderr,"Tube deja cree : '%s' \n", nomTubePid);
		// exit(1);
	} else printf("Creation du tube nomme : '%s' \n", nomTubePid);  
	
	int tRec;
	if((tRec = open(nomTubePid, O_RDONLY)) < 0) {
		fprintf(stderr,"Probleme d'ouverture du tube '%s'\n", nomTubePid);
		exit(1);
	}
	char c;
	while(read(tRec, &c, 1) == 1 && c != '\n') {
			
		putchar(c);
	}
	putchar('\n');
	
	close(tRec);
}
