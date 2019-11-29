package messagebox;

import java.util.*;

import messagebox.Filter;
import messagebox.Message;
/**
 * class MessageBox
 *  boite aux lettres
 *  pour la communication de threads
 */
public class MessageBox {
	
	private List<Message> messages;
	private HashMap<Thread, Thread> blockedThreads;

	/**
	 * constructeur
	 **/
	public MessageBox() {

		this.messages = new ArrayList<>();
		this.blockedThreads = new HashMap<>();
	};


	/**
	 * Depose un message dans la boite aux lettres
	 * @param mes le message a deposer
	 **/
	public  void  deposit(Message mes) {

		synchronized (this.messages) {

			this.messages.add(mes);
			if(this.blockedThreads.get(mes.getSender()) != null) {
				this.blockedThreads.get(mes.getSender()).notify(); // réveille le récepteur
				this.blockedThreads.remove(mes.getSender());
			}
		}
	};

	/**
	 * extrait le plus viel objet dans la MessageBox 
	 *         correspondant a un fitre
	 * @param f filtre a appliquer (eventuellement null)
	 * @return le message extrait
	 **/
	public  Message receive(Filter f) {
		
		Message ret = this.tryReceive(f);
		while(ret == null) {

			synchronized (Thread.currentThread()) {

				try {
					this.blockedThreads.put(Thread.currentThread(), f.getSender());
					Thread.currentThread().wait();
					ret = tryReceive(f);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
		return ret;
	};

	/**
	 * test si la MessageBox contient un message qui repond a un fitre donne
	 *@param f filtre a appliquer (eventuellement null)
	 *@return Message  en cas de succes (null sinon)
	 **/
	public  Message tryReceive(Filter f) {
		
		Message ret = null;

		synchronized (this.messages) {

			if(this.messages.size() > 0) {

				if(f.getSender() != null) {
					for(Message m : this.messages) {
						if(f.isGood(m)) {
							ret = m;
							this.messages.remove(m);
							break;
						}
					}
				} else {
					ret = this.messages.get(0);
				}
			}
		}

		return ret;
	};



}

