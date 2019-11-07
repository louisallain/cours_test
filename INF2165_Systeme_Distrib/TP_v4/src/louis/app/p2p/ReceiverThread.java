package louis.app.p2p;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReceiverThread extends Thread {
	
	/**
	 * Le pair concerné par le Thread d'envoi.
	 */
	private Pair pair;
	
	/**
	 * Constructeur d'objet ReceiverThread.
	 * @param pair le pair concerné par ce thread.
	 */
	public ReceiverThread(Pair pair) {
		this.pair = pair;
	}
	
	@Override
    public void run() {

            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(pair.getUDPPort());
                socket.setBroadcast(true);
            } 
            catch (SocketException ex) {
                ex.printStackTrace();
            }

            byte receivedBytesBuffer[] = new byte[128];
            DatagramPacket packet = new DatagramPacket(receivedBytesBuffer, receivedBytesBuffer.length);
            
            while (true) {
                try {
                    
                    socket.receive(packet);
                    byte receivedData[] = packet.getData();
                    byte receivedCode = receivedData[0];
                    
                    // message de type OFFER
                    if(receivedCode == 1) {

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        // Adresse IP
                        bos.write(receivedData, 1, 4);
                        InetAddress receivedIPAddr = InetAddress.getByAddress(bos.toByteArray());
                        bos.reset();

                        // Port UDP
                        bos.write(receivedData, 5, 2);
                        short receivedUDPPort = ByteBuffer.wrap(bos.toByteArray()).getShort();
                        bos.reset();

                        // Taille user2name
                        byte receivedUser2NameLength = receivedData[7];
                        
                        // User2Name
                        bos.write(receivedData, 8, receivedUser2NameLength);
                        String receivedUser2Name = new String(ByteBuffer.wrap(bos.toByteArray()).array());
                        bos.reset();

                        // Taille filename
                        byte receivedFilenameLength = receivedData[7+receivedUser2NameLength+1];

                        // Filename
                        bos.write(receivedData, (7+receivedUser2NameLength+2), receivedFilenameLength);
                        String receivedFilename = new String(ByteBuffer.wrap(bos.toByteArray()).array());

                        // Traitement de la réponse
                        Path tmpPath = Paths.get(pair.getReceivedFilesDir().toString(), receivedFilename);

                        // si on n'a pas le fichier on le demande, message de type 'request'
                        if(!tmpPath.toFile().exists()) {

                            byte[] requestMsg = Pair.buildRequestMessageBytes(InetAddress.getLocalHost(), pair.getTCPPort(), receivedUser2NameLength, receivedUser2Name, receivedFilenameLength, receivedFilename);
                            
                            DatagramSocket udpSocket = new DatagramSocket();

                            // envoi le message
                            DatagramPacket request =  new DatagramPacket(requestMsg, requestMsg.length, receivedIPAddr, receivedUDPPort);
                            udpSocket.send(request);
                            System.out.println("Message type 'request' envoye");
                            udpSocket.close();
                            //Thread.sleep(50);
                        }
                        // si on a déjà le fichier on demande à le supprimer, message de type 'delete'
                        else {

                            byte[] deleteMsg = Pair.buildDeleteMessageBytes(receivedUser2NameLength, receivedUser2Name, receivedFilenameLength, receivedFilename);
                            
                            DatagramSocket udpSocket = new DatagramSocket();

                            // envoi le message
                            DatagramPacket delete =  new DatagramPacket(deleteMsg, deleteMsg.length, receivedIPAddr, receivedUDPPort);
                            udpSocket.send(delete);
                            System.out.println("Message type 'delete' envoye");
                            udpSocket.close();
                            //Thread.sleep(50);
                        }
                    }
                    // message de type REQUEST
                    else if(receivedCode == 2) {

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        // Adresse IP
                        bos.write(receivedData, 1, 4);
                        InetAddress receivedIPAddr = InetAddress.getByAddress(bos.toByteArray());
                        bos.reset();

                        // Port TCP
                        bos.write(receivedData, 5, 2);
                        short receivedTCPPPort = ByteBuffer.wrap(bos.toByteArray()).getShort();
                        bos.reset();

                        // Taille user2name
                        byte receivedUser2NameLength = receivedData[7];
                        
                        // User2Name
                        bos.write(receivedData, 8, receivedUser2NameLength);
                        String receivedUser2Name = new String(ByteBuffer.wrap(bos.toByteArray()).array());
                        bos.reset();

                        // Taille filename
                        byte receivedFilenameLength = receivedData[7+receivedUser2NameLength+1];

                        // Filename
                        bos.write(receivedData, (7+receivedUser2NameLength+2), receivedFilenameLength);
                        String receivedFilename = new String(ByteBuffer.wrap(bos.toByteArray()).array());

                        // Envoi du fichier
                        new louis.app.file.Client().launch(receivedIPAddr.getHostName(), receivedTCPPPort, Paths.get(pair.getToSendFilesDir().toString(), receivedUser2Name, receivedFilename).toString());
                    }
                    // message de type DELETE
                    else if(receivedCode == 3){

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        
                        // taille username
                        byte receivedUser2NameLength = receivedData[1];

                        // username
                        bos.write(receivedData, 2, receivedUser2NameLength);
                        String receivedUser2Name = new String(ByteBuffer.wrap(bos.toByteArray()).array());
                        bos.reset();

                        // Taille filename
                        byte receivedFilenameLength = receivedData[1+receivedUser2NameLength+1];

                        // Filename
                        bos.write(receivedData, (1+receivedUser2NameLength+2), receivedFilenameLength);
                        String receivedFilename = new String(ByteBuffer.wrap(bos.toByteArray()).array());
                        
                        // Supprime le fichier
                        Paths.get(pair.getToSendFilesDir().toString(), receivedUser2Name, receivedFilename).toFile().delete();
                    }
                    // message de type BEACON
                    else if(receivedCode == 4) {
                    	
                    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    	
                    	// Adresse IP
                    	bos.write(receivedData, 1, 4);
                        InetAddress receivedIPAddr = InetAddress.getByAddress(bos.toByteArray());
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
                        
                        // Vérifie si on connaît le pair ou non
                        if(pair.getKnownPairs().containsKey(receiveUsername)) {
                        	
                        	// pair connu
                        } else {
                        	
                        	// créer la boite d'envoi du nouveau pair
                        	Path newSenderBoxPath = Paths.get(pair.getToSendFilesDir().toString(), receiveUsername);
                        	newSenderBoxPath.toFile().mkdir();
                        	// construit un nouveau thread s'occupant de regarder le dossier d'envoi du nouvel utilisateur
                        	Sender sender = new Sender(pair, newSenderBoxPath);
                        	// pour chaque fichier se trouvant déjà dans la boite d'envoi du nouveau pair, envoie un message de type offer
                        	sender.sendAllOfferMessageTo(receiveUsername, receivedIPAddr, receivedUDPPPort);
                        	// lance un nouveau SenderThread permettant de surveiller s'il y a des fichiers à envoyer
                        	sender.start();
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                	socket.close();
                }
            }
        }
}
