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
	// CREATION DU TUBE
	if (mkfifo("/tmp/tube", mode) == -1) {
		fprintf(stderr,"Probleme d'ouverture du tube nomme (il existe déjà un tube du même nom ?)\n");
		exit(1);
	} 
	else printf("Creation du tube nomme : '/tmp/tube' \n");  
	
	
	// OUVERTURE DU TUBE
	if ((t=open("/tmp/tube",O_RDONLY))< 0) {
		fprintf(stderr,"Probleme d'ouverture du tube en lecture\n");
		exit(1);
	}    
	printf("apres open conso\n");
    
    char cUpperCase;
	while(read(t,&c,1) ==1) {
		cUpperCase = toupper(c);
		write(1, &cUpperCase, 1);
	}
	close(t);
}
