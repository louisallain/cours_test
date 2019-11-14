#include <unistd.h> // fonction pipe
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

// tableau des deux descriteurs correspondants a un tube
// tube[0] lecture
// tube[1] ecriture 
int tube[2];

// progamme principale
int main ( int argc , char **argv ) {

	int pid, nb, t;
	char c;  
	mode_t mode=S_IRUSR|S_IWUSR|S_IRGRP;
	// CREATION DU TUBE
	if (mkfifo("/tmp/tube", mode) == -1) {
		fprintf(stderr,"Probleme d'ouverture du tube nomme\n");
		exit(1);
	} 
	else printf("Creation du tube nomme : '/tmp/tube' \n");  
	
	
	// OUVERTURE DU TUBE
	if ((t=open("/tmp/tube",O_RDONLY))< 0) {
		fprintf(stderr,"Probleme d'ouverture du tube en lecture\n");
		exit(1);
	}    
	printf("apres open conso\n");
    
	while(read(t,&c,1) ==1) {
		write(1,&c,1);
	}
	close(t);
}
