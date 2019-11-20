
class Test extends Thread {
	
	private Barrier bar;
	private String mot;
	private Test next;
	
	public Test(Barrier b, String m, Test next) {
		this.bar = b;
		this.mot = m;
		this.next = next;
	}
	
	public String getMot() {
		return this.mot;
	}
	
	public Test getNext() {
		return this.next;
	}
	
	public void setNext(Test next) {
		this.next = next;
	}
	
	public void run() {
		
		try {
			while(true) {
				System.out.println(this.mot + " : attend");
				this.bar.synchr();
				System.out.println(this.mot);
				this.next.notify();			
				this.wait();
			}	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
