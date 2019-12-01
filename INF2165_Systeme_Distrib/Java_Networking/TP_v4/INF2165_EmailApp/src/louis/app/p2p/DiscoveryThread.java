package louis.app.p2p;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;

public class DiscoveryThread extends Thread {
	
	/**
	 * Le pair concerné par le Thread d'envoi.
	 */
	private Pair pair;
	
	/**
	 * Constructeur d'objet DiscoveryThread.
	 * @param pair le pair concerné par ce thread.
	 */
	public DiscoveryThread(Pair pair) {
		this.pair = pair;
	}
	
	/**
	 * Envoi périodiquement un message de type Discovery en broadcast afin d'avertir les autres pairs
	 * que ce pair existe.
	 * La période est de 5s. (TODO : voir si période OK).
	 */
	@Override
    public void run() {

		this.sender(this.pair);
		this.receiver(this.pair);
	}

	public void sender(Pair pair) {

		(new Thread() {

			@Override
			public void run() {

				while(true) {

					try (DatagramSocket udpSocket = new DatagramSocket()) {

						String username = pair.getUsername();
						int usernameLength = username.getBytes().length;

						// construit le message 'offer'
						byte[] offerMsg = Pair.buildBeaconMessageBytes(InetAddress.getLocalHost(), pair.getUDPPort(), usernameLength, username);

						// envoi le messagebe
						DatagramPacket beacon = new DatagramPacket(offerMsg, offerMsg.length, InetAddress.getByName("255.255.255.255"), 9999);
						udpSocket.send(beacon);
					} catch (SocketTimeoutException ste) {
						System.out.println("Le pair a mis trop longtemps a répondre (Délai expire).");
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						// Période de 5s
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void receiver(Pair pair) {

		(new Thread() {

			@Override
			public void run() {

				DatagramSocket socket = null;
				try {
					socket = new DatagramSocket(9999);
					socket.setBroadcast(true);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}

				byte receivedBytesBuffer[] = new byte[128];
				DatagramPacket packet = new DatagramPacket(receivedBytesBuffer, receivedBytesBuffer.length);

				while(true) {

					try {
						socket.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
					byte receivedData[] = packet.getData();
					byte receivedCode = receivedData[0];
					if (receivedCode == 4) {

						ByteArrayOutputStream bos = new ByteArrayOutputStream();

						// Adresse IP
						bos.write(receivedData, 1, 4);
						InetAddress receivedIPAddr = null;
						try {
							receivedIPAddr = InetAddress.getByAddress(bos.toByteArray());
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
						bos.reset();

						// Port UDP
						bos.write(receivedData, 5, 2);
						short receivedUDPPPort = ByteBuffer.wrap(bos.toByteArray()).getShort();
						bos.reset();

						// Taille username
						byte receiveUsernameLength = receivedData[7];

						// Username
						bos.write(receivedData, 8, receiveUsernameLength);
						String receiveUsername = new String(ByteBuffer.wrap(bos.toByteArray()).array());
						bos.reset();



						// Vérifie si on connaît le pair ou non (et si le beacon ne provient pas de nous même)
						if (pair.getKnownPairs().containsKey(receiveUsername)) {

							System.out.println("Beacon reçu");
							System.out.println("Connu");
							System.out.println("Username : " + receiveUsername);
							// pair connu
							// TODO : gérer la façon dont on garde le pair en mémoire ou non si ça fait longtemps qu'il envoie pas de beacon
						} else if(!pair.getUsername().equals(receiveUsername)){

							System.out.println("Beacon reçu");
							System.out.println("Inconnu");
							// ajoute le nouveau pair à la liste des pairs connus du pair courant
							pair.getKnownPairs().put(receiveUsername, new AbstractMap.SimpleEntry<InetAddress, Integer>(receivedIPAddr, Short.valueOf(receivedUDPPPort).intValue()));
							// créer la boite d'envoi du nouveau pair
							Path newSenderBoxPath = Paths.get(pair.getToSendFilesDir().toString(), receiveUsername);
							newSenderBoxPath.toFile().mkdir();
							// construit un nouveau thread s'occupant de regarder le dossier d'envoi du nouvel utilisateur
							SenderThread senderThread = new SenderThread(pair, newSenderBoxPath);
							// pour chaque fichier se trouvant déjà dans la boite d'envoi du nouveau pair, envoie un message de type offer
							senderThread.sendAllOfferMessageTo(receiveUsername, receivedIPAddr, receivedUDPPPort);
							// lance un nouveau SenderThread permettant de surveiller s'il y a des fichiers à envoyer
							senderThread.start();
						}
					}
				}
			}
		}).start();
	}
}
