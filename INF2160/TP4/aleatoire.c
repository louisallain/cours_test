//    ramdom.c
//
// exemple d'utilisation du generateur de nombres aleatoires
//
// Luc Courtrai

#include<sys/time.h>
#include<stdio.h>

int main(void) {

  int i =0;

  int init; // valeur pour initialiser le generateur   

  // recupere l'heure systeme pour initialiser le generateur
  time_t t;  //strut
  init= time(&t);

  srandom(init);  // initialisation du generateur

  while ( i++ <10)
    printf("%ld \n",random());  // genere un nombre aleatoire

}

