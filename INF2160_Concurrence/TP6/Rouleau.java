
public class Rouleau extends Thread {
	
	/*
	 * Le chiffre représentant le rouleau.
	 */
	private int number;
	/*
	 * Le nombre de millisecondes que le Thread attendra avant d'incrémenter le chiffre. 
	 */
	private int waitMillisNumber;
	/*
	 * Booléen permettant de savoir si le Thread s'exécute ou non
	 */
	private boolean isRunning;
	
	public Rouleau(int waitMillisNumber) {
		
		this.number = 0;
		this.waitMillisNumber = waitMillisNumber;
		this.isRunning = true;
	}
	
	public void setIsRunning(boolean isRunning) { this.isRunning = isRunning; }
	
	public boolean isRunning() { return this.isRunning; }
	
	@Override
	synchronized public String toString() { return String.valueOf(this.number); }
	
	@Override
	public void run() {
		
		
		while(this.isRunning) {
			
			this.number = new java.util.Random().nextInt(10);
			//System.out.print(this.toString());
			
			try {
				Rouleau.sleep(this.waitMillisNumber);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.isRunning = false;
	}
	
	public static void main(String[] args) {
		
		Rouleau r1 = new Rouleau(100);
		r1.start();
	}
}
