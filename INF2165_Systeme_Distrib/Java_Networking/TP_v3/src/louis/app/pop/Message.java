package louis.app.pop;

import java.io.File;

/**
 * Cette classe représente le message d'un client du serveur TCP.
 */
public class Message {

    private File file;
    private boolean marked;

    /**
     * Construit un nouveau message.
     * @param file le fichier réel associé à cet objet message.
     * @param marked un booléen "vrai" si le message a été marqué (ie : il devrai être supprimé dans l'état MISE A JOUR).
     */
    public Message(File file, boolean marked) {
        this.file = file;
        this.marked = marked;
    }

    /**
     * Retourne le fichier réel associé à l'objet.
     * @return fichier réel associé à l'objet 
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Dit si le message a été marqué ou non.
     * @return vrai le message a été marqué, faux sinon
     */
    public boolean isMarked() {
        return this.marked;
    }
    
    /**
     * Associe un fichier à l'objet courant.
     * @param file le fichier réel à associer à l'objet
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Marque le message, ou le "démarque".
     * @param m un booléan vrai si le message doit être marqué, faux sinon
     */
    public void setMarked(boolean m) {
        this.marked = m;
    }
}