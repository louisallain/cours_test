package v2;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

/**
 * Classe Abstraite SynchroLecteurRedacteur Definit la synchornisation d'un algo
 * de type lecteurs/redacteur
 **/

abstract class SynchroLecteurRedacteur {

	/**
	 * Lecteurs
	 */
	public abstract void debutLire();

	public abstract void finLire();

	/**
	 * Redacteur
	 */
	public abstract void debutEcrire();

	public abstract void finEcrire();
}

class SynchroLecteurRedacteurWaitNotify extends SynchroLecteurRedacteur {

    private boolean isLock;
    
    public SynchroLecteurRedacteurWaitNotify() {

        isLock = false;
    }


	@Override
	public synchronized void debutLire() {
		if(isLock) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			isLock=true;
		}
	
	}

	@Override
	public synchronized void finLire() {
		isLock=false;
		notify();
	}

	@Override
	public synchronized void debutEcrire() {
		if(isLock) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			isLock=true;
		}
	}

	@Override
	public synchronized void finEcrire() {
		isLock=false;
		notify();
	}

}

/**
 * Application d'affichage de messages publicitaires
 */

/**
 * Afficheur
 */
public class MessagePubV2 extends Thread {

	static SynchroLecteurRedacteur Synchro = new SynchroLecteurRedacteurWaitNotify();

	static String Messages[];
	static final int NBAFF = 3;

	private int id;

	public MessagePubV2(int id) {
		this.id = id;
	}

	public void run() {

		while (true) {

			try {
				// synchro de debut
				Synchro.debutLire();

				if (Messages[id] != null)
					System.out.println(Messages[id]);
				// synchro de fin
				Synchro.finLire();

				Thread.sleep(1000);
			} catch (Exception e) {

			}
			;
		}
	}

	public static void main(String[] args) {
		//Thread.setPriority(10);
		Messages = new String[NBAFF];

		for (int i = 0; i < NBAFF; i++) {

			MessagePubV2 mp = new MessagePubV2(i);
			mp.setPriority(Thread.MIN_PRIORITY);
			mp.start();
			// (new MessagePubGenerique(i)).start();
		}
		//Thread.setPriority(Thread.MAX_PRIORITY);
		BufferedReader Clavier = new BufferedReader(new InputStreamReader(System.in));
		int num;

		System.out.println("Saisir un numero d'afficheur < " + NBAFF);

		try {
			while (true) {

				do {
					num = Integer.parseInt(Clavier.readLine());
				} while (num < 0 || num >= NBAFF);

				// synchro
				Synchro.debutEcrire();

				System.out.println("Saisir le message");

				Messages[num] = Clavier.readLine();

				// liberer la ressource
				Synchro.finEcrire();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // end of try-catch

} // end of main ()
