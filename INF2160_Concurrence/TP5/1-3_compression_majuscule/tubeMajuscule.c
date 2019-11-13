#include <unistd.h> // fonction pipe
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

// progamme principale
int main ( int argc , char **argv ) {

	int tComp, tRes;
	char c;  
	
	// OUVERTURE DES TUBES
	if ((tComp = open("/tmp/tubeMajuscule", O_RDONLY))< 0) {
		fprintf(stderr,"Probleme d'ouverture du tube '/tmp/tubeMajuscule'\n");
		exit(1);
	}    
	printf("conso '/tmp/tubeMajuscule'\n");
	
	if ((tRes = open("/tmp/tubeResult", O_WRONLY))< 0) {
		fprintf(stderr,"Probleme d'ouverture du tube '/tmp/tubeResult'\n");
		exit(1);
	}    
	printf("conso '/tmp/tubeResult'\n");
    
    char cUpperCase;
	while(read(tComp, &c, 1) == 1) { // lit sur tubeCompress
		cUpperCase = toupper(c);
		write(tRes, &cUpperCase, 1); // ecrit sur tubeResult
	}
	close(tComp);
	close(tRes);
}


