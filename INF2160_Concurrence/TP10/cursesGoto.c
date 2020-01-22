#include<curses.h>

int main(){
  int x,y;
  initscr(); // initialise la fenetre
  // efface la fenetre
  clear();
  
  x=COLS/2;  // COLS nombre de colones du terminal 
  y=LINES/2;   // COLS nombre de lignes du terminal
  
  char cur=219; // UN CARRE

  mvaddch(y,x,cur); // Affiche le caractere a la postion  X Y
  
  
  getch();
  endwin();
  }

//gcc -o cursesGoto cursesGoto.c -lcurses -ltermcap
