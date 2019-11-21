import java.util.LinkedList;

public class SuiteMots {

	private Barrier bar;
	private Mot first;
	private Mot current;
	private String[] mots;

	public SuiteMots(String[] mots) {

		this.bar = new Barrier(mots.length);
		this.first = new Mot(this.bar, mots[0], null);
		this.current = this.first;
		this.mots = mots;
	}
	
	public void launch() {

		this.first.start();

		for(int i = 1; i < this.mots.length; i++) {

			Mot tmpMot = new Mot(this.bar, this.mots[i], null);
			this.current.setNext(tmpMot);
			this.current = tmpMot;
			this.current.start();
			if(i == this.mots.length - 1) {
				this.current.setNext(this.first);
				synchronized (this.first) {
					this.first.notify();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		if(args.length < 1) {
			System.out.println("Give at least one word");
			System.exit(1);
		}
		
		SuiteMots suiteMots = new SuiteMots(args);
		suiteMots.launch();
	}
}
