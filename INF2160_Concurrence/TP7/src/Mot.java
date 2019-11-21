
class Mot extends Thread {
	
	private Barrier bar;
	private String mot;
	private Mot next;
	
	public Mot(Barrier b, String m, Mot next) {
		this.bar = b;
		this.mot = m;
		this.next = next;
	}
	
	public void setNext(Mot next) {
		this.next = next;
	}

	@Override
	public void run() {

		synchronized (this) {
			this.bar.synchr(); // attend que les autres Thread "mot" soient start
			while(true) {
				try {
					this.wait();
					System.out.println(this.mot);
					synchronized (this.next) {
						this.next.notify();
					}
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
