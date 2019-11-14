
public class BanditManchot {
	
	/*
	 * Le nombre de rouleau de l'application.
	 */
	private int nbRolls;
	/*
	 * Les rouleaux de l'application.
	 */
	private Rouleau[] rolls;
	/*
	 * Booléen pour savoir si tous les rouleaux s'exécutent ou non.
	 */
	private boolean isRunning;
	
	public BanditManchot(int nbRolls) {
		
		this.nbRolls = nbRolls;
		this.rolls = null;
		this.isRunning = false;
	}
	
	public void launchRolls() {
		
		this.rolls = new Rouleau[this.nbRolls];
		Rouleau tmpR = null;
		
		for(int i = 0; i < this.nbRolls; i++) {
			
			tmpR = new Rouleau(500);
			this.rolls[i] = tmpR;
			this.rolls[i].start();
		}
		this.isRunning = true;
	}
	
	synchronized public void stopRolls() {
		
		if(this.rolls != null) {
			
			for(int i = 0; i < this.nbRolls; i++) {
				
				this.rolls[i].setIsRunning(false);
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.err.println("Erreur, il faut d'abord démarrer les rouleaux.");
		}
		this.isRunning = false;
	}
	
	public boolean isRunning() { return this.isRunning; }
	
	public void setIsRunning(boolean isRunning) { this.isRunning = isRunning; } 
	
	@Override
	synchronized public String toString() { 
		
		String ret = "";
		for(int i = 0; i < this.nbRolls; i++) {
			ret += this.rolls[i].toString() + " ";
		}
		return ret;
	}
	
	public static void main(String[] args) {
		
		BanditManchot bm = new BanditManchot(3);
		bm.launchRolls();
		Afficheur a = new Afficheur(bm);
		a.start();
		try{
			System.in.read();
			bm.stopRolls();
		} catch(Exception e){	
			e.printStackTrace();
		}
	}
}
