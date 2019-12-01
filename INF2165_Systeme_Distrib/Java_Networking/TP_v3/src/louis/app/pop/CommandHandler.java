package louis.app.pop;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import louis.app.ubs.LockableDirectory;

/**
 * Cette classe permet au serveur POP3 de traiter les commandes que les clients envoient.
 */
public class CommandHandler {

    private State state;
    private String username;
    private String password;
    private Path directory;
    private Path userDirectory;
    private HashMap<Integer, Message> messages;

    /**
     * Enumeration des états possibles du protocol POP3.
     */
    private enum State {

        AUTH, TRANS, UPDATE;
    };

    /**
     * Construit un nouvel objet CommandHandler. 
     * @param d le dossier du client (là où sont sauvegardés tous ses messages).
     */
    public CommandHandler(Path d) {

        this.state = State.AUTH;
        this.username = "";
        this.password = "";
        this.directory = d;
        this.messages = new HashMap<Integer, Message>();
    }

    /**
     * Construit un réponse positive du protocol POP3.
     * @param infos des informations à donner en plus
     * @return un chaine de type "¨+OK <infos>"
     */
    public String positiveResponse(String infos) {
        return "+OK " + infos;
    }

    /**
     * Construit un réponse négative du protocol POP3.
     * @param infos des informations à donner en plus
     * @return un chaine de type "¨-ERR <infos>"
     */
    public String negativeResponse(String infos) {
        return "-ERR " + infos;
    }

    /**
     * Donne le nombre de messages du client.
     * @return le nombre de messages du client
     */
    public int getNbMsgs() {
        Path userMsgsPath = Paths.get(this.userDirectory.toString(), "/msgs");
        return userMsgsPath.toFile().list().length;
    }

    /**
     * Retourne la taille d'un dossier en octets.
     * @param dir un dossier
     * @return la taille en octets.
     */
    public long folderSize(File dir) {
        
        long length = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    /**
     * Retourne la taille de tous les messages du client en octets.
     * @return la taille de tous les messages du client en octets
     */
    public long getNbBytesMsgs() {
        Path userMsgsPath = Paths.get(this.userDirectory.toString(), "/msgs");
        
        return folderSize(userMsgsPath.toFile());
    }

    /**
     * Donne le résultat sous forme d'une chaine de caractère d'une commande client conformément au RFC 1939 du protocol POP3.
     * @param commandReceived une commande client
     * @return le résultat sous forme d'une chaine de caractère d'une commande client conformément au RFC 1939 du protocol POP3
     */
    public String getResultOfCommand(String commandReceived) {

        // QUIT
        if(commandReceived.matches("quit") || commandReceived.matches("QUIT")) {

            if(this.state == State.AUTH) {
                return this.positiveResponse("quit ok"); 
            }
            else {
                this.state = State.UPDATE;
                
                // mets à jour le dossiers des messages réellement
                this.messages
                .values()
                .stream()
                .filter(msg -> msg.isMarked() == true)
                .forEach(msg -> msg.getFile().delete());

                long nbMarkedMsg = this.messages
                .values()
                .stream()
                .filter(msg -> msg.isMarked() == true)
                .count();
                
                // Déverrouille le dossier
                try {
                    LockableDirectory userMailboxLockableDir = new LockableDirectory(this.userDirectory.toFile());
                    userMailboxLockableDir.releaseLock();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                if((this.getNbMsgs() + nbMarkedMsg) == this.messages.size()) {
                    return this.positiveResponse("all marked messages were deleted");
                }
                else {
                    return this.negativeResponse("some marked messages were not deleted");
                }
            }
        }
        // STAT
        else if(commandReceived.matches("stat") || commandReceived.matches("STAT")) {

            if(this.state != State.TRANS) {
                return this.negativeResponse("illegal state for this command");
            }
            else {
                int nbMsgs = this.getNbMsgs();
                long sizeMsgs = this.getNbBytesMsgs();
                
                return this.positiveResponse(String.valueOf(nbMsgs) + " " + String.valueOf(sizeMsgs));
            }
        }
        // LIST
        else if(commandReceived.matches("list ?.{0,40}") || commandReceived.matches("LIST .{0,40}")) {
            if(this.state != State.TRANS) {
                return this.negativeResponse("illegal state for this command");
            }
            if(commandReceived.matches("list") || commandReceived.matches("LIST")) {
                int nbMsgs = this.getNbMsgs();
                long sizeMsgs = this.getNbBytesMsgs();
                
                String ret = this.positiveResponse(String.valueOf(nbMsgs) + " " + String.valueOf(sizeMsgs)) + "\r\n";
                for(int i = 0; i < this.messages.size(); i++) {
                    Message currentMessage = this.messages.get(i);
                    
                    if(currentMessage.isMarked() == false) {
                        ret = ret + i + " " + currentMessage.getFile().length() + "\r\n";
                    }
                }
                ret = ret + ".";
                return ret;
            }
            else if(commandReceived.matches("list \\d+") || commandReceived.matches("LIST \\d+")) {
                int id = Integer.parseInt(commandReceived.replaceFirst("list |LIST  ", ""));
                if(this.messages.containsKey(id) == true) {
                    if(this.messages.get(id).isMarked() == false) {
                        return this.positiveResponse(id + " " + this.messages.get(id).getFile().length());
                    }
                }
                else {
                    return this.negativeResponse("message does not exists");
                }
            }
            else {
                return this.negativeResponse("wrong arguments");
            }
        }
        // RETR
        else if(commandReceived.matches("retr \\d+") || commandReceived.matches("RETR \\d+")) {
            if(this.state != State.TRANS) {
                return this.negativeResponse("illegal state for this command");
            }

            int id = Integer.parseInt(commandReceived.replaceFirst("retr |RETR  ", ""));
            if(this.messages.containsKey(id) == true) {
                if(this.messages.get(id).isMarked() == false) {
                    String ret = this.positiveResponse(this.messages.get(id).getFile().length() + " bytes\r\n");
                    try (
                        BufferedReader br = new BufferedReader(new FileReader(this.messages.get(id).getFile()));
                    ){
                        String line;
                        while((line = br.readLine()) != null) {
                            if(line.startsWith(".")) {
                                line = "." + line;
                            }
                            ret = ret + line + ".\r\n";
                        }
                        br.close();
                        ret = ret + "\r\n";
                        return ret;
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                return this.negativeResponse("message does not exists");
            }
        }
        // DELE
        else if(commandReceived.matches("dele \\d") || commandReceived.matches("DELE \\d")) {
            if(this.state != State.TRANS) {
                return this.negativeResponse("illegal state for this command");
            }
            int id = Integer.parseInt(commandReceived.replaceFirst("dele |dele  ", ""));
            if(this.messages.containsKey(id) == true) {
                if(this.messages.get(id).isMarked() == false) {
                    this.messages.get(id).setMarked(true);
                    return this.positiveResponse("message " + id + " marked");
                }
                else {
                    return this.negativeResponse("message already marked");
                }
            }
            else {
                return this.negativeResponse("message does not exists");
            }
        }
        // NOOP
        else if(commandReceived.matches("noop") || commandReceived.matches("NOOP")) {
            if(this.state != State.TRANS) {
                return this.negativeResponse("illegal state for this command");
            }
            else {
                return this.positiveResponse("noop");
            }
        }
        // RSET
        else if(commandReceived.matches("rset") || commandReceived.matches("RSET")) {
            if(this.state != State.TRANS) {
                return this.negativeResponse("illegal state for this command");
            }
            else {
                this.messages.forEach((id, msg) -> {
                    msg.setMarked(false);
                });
                return this.positiveResponse("all marked messages are now unmarked");
            }
        }
        // USER
        else if(commandReceived.matches("user .{0,40}") || commandReceived.matches("USER .{0,40}")) {
            
            if(this.state != State.AUTH) {
                return this.negativeResponse("illegal state for this command");
            }
            // récupère "username" depuis la commande reçue
            String tmpUsername = commandReceived.replaceFirst("user |USER  ", "");
            // vérifie si une boîte au lettre à ce nom existe
            Path tmpUserDirectory = Paths.get(this.directory.toString(), tmpUsername);
            if(Files.exists(tmpUserDirectory) == true) {
                this.username = tmpUsername;
                this.userDirectory = tmpUserDirectory;
                // vérifie si son dossier n'est pas verrouillé
                try {
                    LockableDirectory userDirectoryLockable = new LockableDirectory(this.userDirectory.toFile());
                    if(userDirectoryLockable.hasLock() == false) {
                        return this.positiveResponse("user mailbox exists");
                    }
                    else {
                        return this.negativeResponse("user mailbox locked");
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                
            }
            else {
                return this.negativeResponse("user mailbox doesn't exist");
            }
        }
        // PASS
        else if(commandReceived.matches("pass .{0,40}") || commandReceived.matches("PASS .{0,40}")) {
            
            String tmpPassword = commandReceived.replaceFirst("pass |PASS  ", "");
            if(this.state != State.AUTH) {
                return this.negativeResponse("illegal state for this command");
            }
            else if(tmpPassword.length() < 4) {
                return this.negativeResponse("password length < 4");
            }
            else if(this.username.length() == 0) {
                return this.negativeResponse("pls give username first with USER");
            }

            // vérifie si les mots de passe correspondent
            Path userMailboxPasswordFile = Paths.get(this.userDirectory.toString(), "mdp/password.txt");
            String truePassword = "";
            try (
                BufferedReader mdpbr = new BufferedReader(new FileReader(userMailboxPasswordFile.toFile()));
            ){
                truePassword = mdpbr.readLine();
                mdpbr.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            
            if(tmpPassword.equals(truePassword)) {
                // rempli la liste des messages
                Path userMsgsPath = Paths.get(this.userDirectory.toString(), "/msgs");
                File userMsgsDir = userMsgsPath.toFile();
                File[] listOfMsgs = userMsgsDir.listFiles();
                
                for(int i = 0; i < listOfMsgs.length; i++) {
                    this.messages.put(i, new Message(listOfMsgs[i], false));
                }
                // change d'état
                this.state = State.TRANS;
                // verrouille le dossier de l'utilisateur
                boolean lockedOk = false;
                try {
                    LockableDirectory userMailboxLockableDir = new LockableDirectory(this.userDirectory.toFile());
                    lockedOk = userMailboxLockableDir.acquireLock();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
                if(lockedOk == true) {
                    return this.positiveResponse("password checked, mailbox locked for transaction");
                }
                return this.negativeResponse("mailbox already locked");
            }
            else {
                return this.negativeResponse("password incorrect");
            }
        }
        return this.negativeResponse("unknown command");
    }
}