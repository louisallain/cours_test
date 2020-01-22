 
#include<curses.h>
#include<stdio.h>

int main(int argc, char **argv){
  
 
  char c;
   
  initscr();  // initislise le terminal
 
  
  noecho(); // pas de retour ecran  (saisie masquee)
  while ( (c = getch()) != '\n') {
	 
	switch (c) { 
		case 56 : printf("H"); break; 
		case 54 : printf("D"); break; 
		case 52 : printf("G"); break; 
		case 50 : printf("B"); break; 
	}
	echo();  // retour ecran
	 
	fflush(stdout);
	noecho();  // pas de retour ecran 
  
  
  }
	    	  
   
   //fflush(stdout);
    endwin();
 
  };
  

/*
gcc -o fleche fleche.c -lcurses -ltermcap
*/
