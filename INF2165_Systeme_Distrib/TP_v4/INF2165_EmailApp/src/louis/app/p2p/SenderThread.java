package louis.app.p2p;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.stream.Stream;

public class SenderThread extends Thread {
	
	/**
	 * Le pair concerné par le Thread d'envoi.
	 */
	private Pair pair;
	
	/**
	 * Le dossier surveillé par l'activité.
	 */
	private Path dirWatched;
	
	/**
	 * Constructeur d'objet SenderThread.
	 * @param pair le pair concerné par ce thread.
	 */
	public SenderThread(Pair pair, Path dirToBeWatched) {
		this.pair = pair;
		this.dirWatched = dirToBeWatched;
	}
	
	/**
	 * Pour chaque fichier se trouvant dans le dossier du pair en paramètre pour le pair concerné par ce thread,
	 * on envoie un message de type 'offer'.
	 * @param username le nom d'utilisateur à qui envoyer les message d'offre
	 */
	public void sendAllOfferMessageTo(String username, InetAddress ip, short udp) {
		
		Path sendBoxPath = Paths.get(pair.getToSendFilesDir().toString(), username);
		try(Stream<Path> paths = Files.walk(sendBoxPath)) {

	        paths
	            .filter(Files::isRegularFile)
	            .forEach(file -> {
	            	try (DatagramSocket udpSocket = new DatagramSocket()){
	            		
	            		// construit le message 'offer'
	                    byte[] offerMsg = Pair.buildOfferMessageBytes(InetAddress.getLocalHost(), pair.getUDPPort(), username.length(), username, file.toString().length(), file.toString());
	                    
	                    // envoi le message
	                    DatagramPacket offer =  new DatagramPacket(offerMsg, offerMsg.length, ip, udp);
	                    udpSocket.send(offer);
	                    System.out.println("Message type 'offer' envoye a " + username);
	            	} catch(Exception e) {
	            		e.printStackTrace();
	            	}
	            	
	            });
	        }
	        catch(Exception e) {
	            e.printStackTrace();
	        }
	}
	
	/**
    * Met en place une surveillance sur le dossier en paramètre (uniquement sur la création).
    * @param watcher le "watcher associé"
    * @param dir le dossier a surveillé
    */
	private void watchForCreation(WatchService watcher, Path dir) {
		try {
			dir.register(watcher, ENTRY_CREATE);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Surveille le dossier d'envoi du pair concerné par ce Thread.
	 * A chaque fois qu'un fichier est créé dans ce dossier d'envoi, le Thread envoi un message
	 * de type offer au nom du dossier d'envoi (ie : un autre pair).
	 */
	@Override
    public void run() {

        // Surveille le dossier d'envoi
        WatchService watcher;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            this.watchForCreation(watcher, this.dirWatched);
        }
        catch(Exception e) {
            e.printStackTrace();
            return;
        }

        // On boucle sans fin en attendant un évènement provenant
        // du dossier d'envoi du pair
        while(true) {

            WatchKey key;
            try {
                key = watcher.take();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Parcours les évènements survenus (s'il y en a)
            for(WatchEvent<?> event : key.pollEvents()) {

                WatchEvent.Kind<?> kind = event.kind();

                // Gère les types OVERFLOW au cas où
                if(kind == OVERFLOW) continue;
                
                // Traite l'event
				WatchEvent<Path> ev = (WatchEvent<Path>)event;
                // Le nom du fichier est le context de l'event
                Path newFile = ev.context();
                // Retrouve le chemin complet
                Path tmp = (Path)key.watchable();
                Path newFileFullPath = tmp.resolve(newFile);

                // Cas d'un nouveau fichier à envoyer
                File tmpFile = Paths.get(pair.getToSendFilesDir().toString(), newFile.toString()).toFile();
                if(newFileFullPath.toFile().isFile()) {

                    // Récupère les infos de l'utilisateur concerné
                    String user = newFileFullPath.getParent().getFileName().toString();
                    System.out.println("Nouveau mail (" + newFile +") a envoyer a " + user);
                     
                    if(pair.getKnownPairs().containsKey(user)) {
                    	
                    	InetAddress toSentIP = pair.getKnownPairs().get(user).getKey();
                        int toSentPort = pair.getKnownPairs().get(user).getValue();
                        int usernameLength = user.getBytes().length;
                        int filenameLength = newFile.toString().getBytes().length;
                                                    
                        try (DatagramSocket udpSocket = new DatagramSocket()){

                            // construit le message 'offer'
                            byte[] offerMsg = Pair.buildOfferMessageBytes(InetAddress.getLocalHost(), pair.getUDPPort(), usernameLength, user, filenameLength, newFile.toString());
                            
                            // envoi le message
                            DatagramPacket offer =  new DatagramPacket(offerMsg, offerMsg.length, toSentIP, toSentPort);
                            udpSocket.send(offer);
                            System.out.println("Message type 'offer' envoye a " + user);
                            //Thread.sleep(50);
                        }
                        catch (SocketTimeoutException ste) {
                            System.out.println("Le pair a mis trop longtemps a répondre (Délai expire).");
                        } 
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                    	System.out.println("Erreur : pair inconnu.");
                    }
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
	}
}
