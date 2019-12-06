import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

import java.util.concurrent.Semaphore;

/**
 * Classe Abstraite SynchroLecteurRedacteur
 *     Definit la synchornisation d'un algo de type lecteurs/redacteur
 **/
 
abstract class SynchroLecteurRedacteur{
	
	 
    /**
     * Lecteurs
     */
	public abstract void debutLire( );
    public abstract void finLire( );
    
	/**
	 * Redacteur
	 */
	public abstract void debutEcrire( );
	public abstract void finEcrire( );
}
	

 
class SynchroLecteurRedacteurWaitNotify extends SynchroLecteurRedacteur {

    private Semaphore sMutex, sLect;
    private int nbLect;

    public SynchroLecteurRedacteurWaitNotify() {

        sLect = new Semaphore(1, true);
        sMutex = new Semaphore(1, true);
        
        nbLect = 0;
    }

    @Override
    public void debutLire() {

		sLect.acquireUninterruptibly();
		nbLect++;
		if(nbLect == 1) sMutex.acquireUninterruptibly();
		sLect.release();
    }
    @Override
    public void finLire() {

        sLect.acquireUninterruptibly();
		nbLect--;
		if (nbLect == 0) {
			sMutex.release();
		}
		sLect.release();
    }

    @Override
    public void debutEcrire() {
        sMutex.acquireUninterruptibly();
    }

    @Override
    public void finEcrire() {

        sMutex.release();
    }
}



/**
 * Application d'affichage de  messages publicitaires
 */
 
/**
 * Afficheur
 */
public  class MessagePub extends Thread{

  
	
	static SynchroLecteurRedacteur Synchro = new SynchroLecteurRedacteurWaitNotify();
	static String messages[];
	static final int NBAFF = 3;


	private  int id;

	public MessagePub(int id){
		this.id = id;

	}


	public void run() {

		while(true) {


			try {
				// synchro de debut
				Synchro.debutLire();

                if (messages[id] != null) System.out.println(messages[id]);
                
				//  synchro de fin
				Synchro.finLire();

				Thread.sleep(1000);
			} catch ( Exception  e) {

			} ;  
		}
	}


	public static void main (String[] args)  {

		messages = new String[NBAFF];

		for (int i=0; i < NBAFF;i++){

            MessagePub mp = new MessagePub(i);
			mp.start();
			// (new MessagePubGenerique(i)).start();
		}

		BufferedReader Clavier = new BufferedReader(new InputStreamReader(System.in));
		int num;

		System.out.println("Saisir un numero d'afficheur < "+NBAFF);

		try {

			while(true) {

				do {
					num =  Integer.parseInt(Clavier.readLine());
				} while (num < 0 || num >= NBAFF);

				// synchro 
				Synchro.debutEcrire();

				System.out.println("Saisir le message");

				messages[num]=Clavier.readLine();	

				// liberer la ressource
				Synchro.finEcrire();
			}

		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } // end of try-catch
    
} // end of main ()


