#include <curses.h>

int main() {
	
	int x, y;
	initscr();
	clear();
	x = COLS/2;
	y = LINES/2;
	char cur = 219;
	mvaddch(y, x, cur);
	getch();
	endwin();
}
