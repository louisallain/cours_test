// jeuStarWars.c
//

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include <pthread.h>

#include <curses.h>
#include <unistd.h>

int xJedi = 0;
int yJedi = 0;

void * etoile() {
	
	pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
	
	char cur=219;
	
	

	int x  = rand() % COLS ;
	int y  = rand() % LINES ;
	
	int dx=rand() % 2;
	if (dx ==0) dx=-1;
	int dy=rand() % 2;
	if (dy ==0) dy=-1;


	while(1) {
		
		while (x >0 && x < COLS && y >0 && y < LINES) {
			
			pthread_mutex_lock(&lock);
			
			mvaddch(y,x,'*');
			refresh();
			
			int rSleep = (rand() % 100000) + 900000;
			usleep(rSleep);
			mvaddch(y,x,' ');
			
			pthread_mutex_unlock(&lock);
			
			x+=dx;
			y+=dy;
		}
		
		// regarde si le jedi est pas plus fort
		if(xJedi == x && yJedi == y) pthread_exit(1);
		
		if(x == 0) {
			x = x + 1;
			dx = 1;
		}
		else if(x == COLS) {
			x = x - 1;
			dx = -1;
		}
		else if(y == 0) {
			y = y + 1;
			dy = 1;
		}
		else if(y == LINES) {
			y = y - 1;
			dy = -1;		
		}
		
		// créer une nouvelle étoile
		pthread_t etoile_id1;
	
		if(pthread_create(&etoile_id1, NULL, etoile, (void *)NULL)) {
			fprintf(stderr, "error creating thread\n");
		}
	}
}


// programme principal
int main(void){

	initscr();
	clear();
	
	srand(time(NULL));

	pthread_t etoile_id1;
	pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;
	
	if(pthread_create(&etoile_id1, NULL, etoile, (void *)NULL)) {
		fprintf(stderr, "error creating thread\n");
	}
	
	noecho(); // pas de retour ecran  (saisie masquee)
	
	char c;
	
	while ( (c = getch()) != '\n') {
		
		pthread_mutex_lock(&lock);
		mvaddch(yJedi, xJedi, ' ');
		pthread_mutex_unlock(&lock);
	
		switch (c) { 
			case 56 : yJedi += - 1; break; // h
			case 54 : xJedi += 1; 	break; // d
			case 52 : xJedi += - 1; break; // g
			case 50 : yJedi += + 1; break; // b
		}
		
		pthread_mutex_lock(&lock);
		mvaddch(yJedi, xJedi, 219);
		pthread_mutex_unlock(&lock);
	
		echo();  // retour ecran
		noecho();  // pas de retour ecran 
	}
	

	// attente des threads
	pthread_join(etoile_id1, NULL);	
	
	
}

// Compilation et edition des liens
// gcc jeuStarWarModel.c   -lcurses -ltermcap







