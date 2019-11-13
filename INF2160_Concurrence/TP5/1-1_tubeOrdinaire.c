#include <unistd.h> // fonction pipe
#include <stdio.h>

// tableau des deux descriteurs correspondants a un tube
// tube[0] lecture
// tube[1] ecriture 
int tube[2];

// progamme principale
int main ( int argc , char **argv ) {

  int pid; // numero du fil
  char c;  
  
  // creation du tube
  // NR : toujours avant la creation d'un fils
  // les deux processus pere et fils auront une copie
  // des descripteurs 
  if (pipe(tube) == -1) { // -1 erreur
    fprintf(stderr,"Probleme d'ouverture du tube\n");
    exit(1);
  }    
  // creation d'un processu fils
  if ((pid=fork())== 0) {
    // processus fils 
    // le pere va lire dans le tube
    close(tube[1]);
    // lecture d'un caratere dans le tube
    while(read(tube[0],&c,1) >0)
      // affiche le caratere sur la sortie standard si ce n'est pas un espace
      if(c != 32) write(1,&c,1);
    // ferme le tube
    close(tube[0]);
    // sortie sans erreur
    exit(0);
    
  } else {
    
    // processus pere 
    // le fils va ecrire dans le tube (il peut donc fermer le cote lecture)
    close(tube[0]);
    // lit un caracter sur l'entree standard
    // sortie de la boucle par ctrl D
    while(read(0,&c,1) >0)
      //ecrit dans le tube
      write(tube[1],&c,1);
    // ferme le tube (donc le fils sort de la boucle)
    close(tube[1]);
  }
}
