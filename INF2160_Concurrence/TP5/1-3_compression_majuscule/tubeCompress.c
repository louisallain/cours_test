#include <unistd.h> // fonction pipe
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

// progamme principale
int main ( int argc , char **argv ) {

	int tComp, tMaj;
	char c;  
	
	// OUVERTURE DES TUBES
	if ((tComp = open("/tmp/tubeCompress", O_RDONLY))< 0) {
		fprintf(stderr,"Probleme d'ouverture du tube '/tmp/tubeCompress'\n");
		exit(1);
	}    
	printf("conso '/tmp/tubeCompress'\n");
	
	if ((tMaj = open("/tmp/tubeMajuscule", O_WRONLY))< 0) {
		fprintf(stderr,"Probleme d'ouverture du tube '/tmp/tubeMajuscule'\n");
		exit(1);
	}    
	printf("conso '/tmp/tubeMajuscule'\n");
    
	while(read(tComp, &c, 1) == 1) { // lit sur tubeCompress
		
		if(c != 32) write(tMaj, &c, 1); // ecrit sur tubeMajuscule
	}
	close(tComp);
	close(tMaj);
}

