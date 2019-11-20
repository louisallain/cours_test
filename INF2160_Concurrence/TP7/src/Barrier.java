
public class Barrier {

	private int nbThreads;
	private int cptThreads;
	
	public Barrier(int nbThreads) {
		
		this.nbThreads = nbThreads;
		this.cptThreads = 0;
	}
	
	public synchronized void synchr() {
		
		try {
			
			if(this.cptThreads == this.nbThreads-1) {
				this.cptThreads = 0;
				notifyAll();
			}
			else {
				this.cptThreads++;
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
