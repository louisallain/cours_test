#include <unistd.h> // fonction pipe
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

// progamme principale
int main ( int argc , char **argv ) {

	int pid, nb, t;
	char c;  
	mode_t mode=S_IRUSR|S_IWUSR|S_IRGRP;
	
	
	////////////////////////
 	// CREATION DES TUBES //
	////////////////////////
	
	// tubeCompress
	if (mkfifo("/tmp/tubeCompress", mode) == -1) {
		fprintf(stderr,"Probleme ouverture '/tmp/tubeCompress'\n");
		exit(1);
	} 
	else printf("Creation du tube nomme : '/tmp/tubeCompress' \n");  
	// tubeMajuscule
	if (mkfifo("/tmp/tubeMajuscule", mode) == -1) {
		fprintf(stderr,"Probleme ouverture '/tmp/tubeMajuscule'\n");
		exit(1);
	} 
	else printf("Creation du tube nomme : '/tmp/tubeMajuscule' \n"); 
	// tubeResult
	if (mkfifo("/tmp/tubeResult", mode) == -1) {
		fprintf(stderr,"Probleme ouverture '/tmp/tubeResult'\n");
		exit(1);
	} 
	else printf("Creation du tube nomme : '/tmp/tubeResult' \n"); 
}
