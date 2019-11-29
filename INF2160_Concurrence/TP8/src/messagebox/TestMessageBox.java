package messagebox;

import messagebox.MessageBox;
import messagebox.Filter;
import messagebox.Message;
import java.io.*;



class ThProd extends Thread{

	private MessageBox mb;
	private int delay;

	public ThProd(MessageBox mesBox,int delay){
		mb=mesBox;
		this.delay=delay;
	}          

	public void run(){
		int val=0;
		while (true) {
			String mes=new String(this+" mes : "+new Integer(val++));
			System.out.println(this + "depose "+mes);
			mb.deposit(new Message(mes));
			try {
				Thread.sleep(500+(int)(Math.random()*delay));    
			} catch (InterruptedException e) {}	    

		}
	}

}

class ThCons extends Thread{

	private MessageBox mb;
	private Thread emt;
	private int delay;

	public ThCons(MessageBox mesBox,Thread emt, int del){
		this.mb=mesBox;
		this.emt=emt;
		this.delay=del;
	}          

	public void run(){
		int val=0;
		Filter aFiltre = new Filter (emt);
		while (true) {
			Message m;
			System.out.println(this + "demande message venant de " + emt);
			m = mb.receive(aFiltre);
			System.out.println(this + "extrait "+(String)m.getObject());
			try {
				Thread.sleep(500+(int)(Math.random()*delay));     
			} catch (InterruptedException e) {}	    

		}

	}

}




public class TestMessageBox{


	static public void main(String[] args){

		MessageBox bal = new MessageBox();


		System.out.println("Test fonctionnel de la BAL avec un seul thread"); 
		for (int i=0; i< 10; i++)
			bal.deposit(new Message(new Integer(i)));


		System.out.println("Test extrait les plus vieux  5");

		Filter unFiltre = new Filter(Thread.currentThread());
		for (int i=0; i< 5; i++) {
			System.out.println("Recu :"+ (Integer) bal.receive(unFiltre).getObject());
		}




		System.out.println("Test fonctionnel de la BAL multi thread ");
		System.out.println(" 4 threads   pendant 10 secondes ");
		ThProd prod1 = new ThProd(bal,1000);
		ThCons cons1 = new ThCons(bal,prod1,1000);
		ThProd prod2 = new ThProd(bal,2000);
		ThCons cons2 = new ThCons(bal,prod2,2000);
		cons1.start();
		prod1.start();
		cons2.start();
		prod2.start();



		try {
			Thread.sleep(10000);    // s'execute pendant 5 secondes
		} catch (InterruptedException e) {}

		System.exit(0);



	}
}
