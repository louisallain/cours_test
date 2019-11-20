import java.util.LinkedList;

public class SuiteMots {
	
	private LinkedList<Test> wordThreads;
	private Barrier bar;
	
	public SuiteMots(String[] mots) {
		
		this.wordThreads = new LinkedList<Test>();
		this.bar = new Barrier(mots.length);

		for(int i = 0; i < mots.length; i++) {
		
			Test tmpTest = new Test(this.bar, mots[i], null);
			this.wordThreads.add(tmpTest);
		}
		
		// init les suivants
		for(int i = 0; i < mots.length; i++) {
			
			Test next;
			if(i == mots.length - 1) next = this.wordThreads.get(0);
			else next = this.wordThreads.get(i+1);
			
			this.wordThreads.get(i).setNext(next);
		}
	}
	
	public void launch() {
		this.wordThreads.forEach(e -> e.start());
		this.wordThreads.getFirst().notify();
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
