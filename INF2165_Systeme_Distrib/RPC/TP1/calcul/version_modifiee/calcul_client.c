#include <limits.h>
#include "calcul.h"

CLIENT *clnt;

void
addition (uint param1, uint param2)
{	
  reponse  *resultat;
  data  parametre;
  /* 1. Preparer les arguments */
  parametre.arg1 = param1;
  parametre.arg2 = param2; 
  /* 2. Appel de la fonction distante */
  resultat = calcul_addition_1 (&parametre, clnt);
  if (resultat == (reponse *) NULL) {
    clnt_perror (clnt, "call failed");
    clnt_destroy (clnt);
    exit(EXIT_FAILURE);
  } else if ( resultat->errno == 0 ) {
    printf("Le resultat de l'addition est: %u \n\n",resultat->somme);
  } else {
    printf("La fonction distante ne peut faire l'addition a cause d'un overflow \n\n");
  }
}

int
main (int argc, char *argv[])
{
  if (argc < 4) {
    printf ("usage: %s server_host val1 val2\n", argv[0]);
    exit (1);
  }
  char *host = argv[1];
  int val1= atoi(argv[2]);
  int val2= atoi(argv[3]);
  /* début de la connexion avec le serveur */
  clnt = clnt_create (host, CALCUL, VERSION_UN, "udp");
  if (clnt == NULL) {
    clnt_pcreateerror (host);
    exit (1);
  }
  // test d'une addition correcte
  printf ("rpc addition %d %d on %s\n", val1,val2,host);
  addition ( val1, val2 );
  // test d'une addition avec débordement
  printf ("rpc addition UINT_MAX %d on %s\n", val1,host);
  addition ( UINT_MAX, val1 );
  /* fin de la connexion avec le serveur */
  clnt_destroy (clnt);
  exit(EXIT_SUCCESS);
}
