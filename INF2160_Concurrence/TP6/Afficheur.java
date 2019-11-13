
public class Afficheur extends Thread {
	
	/*
	 * Le nombre de ms entre chaque affichage. 
	 */
	private final static int MILLIS_PRINT = 250;
	
	/*
	 * La machine à sous associé à l'afficheur.
	 */
	private BanditManchot bm;
	
	public Afficheur(BanditManchot bm) {
		
		this.bm = bm;
	}
	
	public static void clearScreen() {  
	    System.out.print("\033[H\033[2J");  
	    System.out.flush();  
	}
	
	@Override
	public void run() {
				
		while(this.bm.isRunning()) {
			
			synchronized(this.bm) {
				
				Afficheur.clearScreen();
				System.out.println(this.bm.toString());
			}
			try {
				Afficheur.sleep(Afficheur.MILLIS_PRINT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}
}
