#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

// Lecture écriture d'un tableau d'entiers à partir d'un fichier
// La tableau possède une taille réelle MAX mais ne peut contenir
// que N entiers (taille effective)
//
// Le premier entier du tableau (indice 0) doit contenir le nombre d'éléments 
// effectifs  du tableau.
// Le premier élément est donc à l'indice 1.
//
// Pour un tableax de max 10 éléments sa taille doit être de 11 (max +1).
//                 tab[0] étant la taille réelle
 
 
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
     fprintf(stderr,"probleme de lecture du fichier %d\n", read(cible,tab,(size+1) * sizeof(int)));
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
