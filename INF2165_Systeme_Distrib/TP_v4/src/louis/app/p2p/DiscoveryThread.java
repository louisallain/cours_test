package louis.app.p2p;

public class DiscoveryThread extends Thread {
	
	/**
	 * Le pair concerné par le Thread d'envoi.
	 */
	private Pair pair;
	
	/**
	 * Constructeur d'objet DiscoveryThread.
	 * @param pair le pair concerné par ce thread.
	 */
	public DiscoveryThread(Pair pair) {
		this.pair = pair;
	}
	
	/**
	 * Envoi périodiquement un message de type Discovery en broadcast afin d'avertir les autres paires 
	 * que ce pair existe.
	 * La période est de 5s. (TODO : voir si période OK).
	 */
	@Override
    public void run() {
		
		
		
		// Période de 5s
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
