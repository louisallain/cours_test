package louis.app.ubs;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A mail following the format defined in RFC 822, read from a
 * file. Some of the attributes in the mail header are initialized by
 * reading the file. 
 */
public class MailFile {

    private File file;         // the file that contains the mail
    private String from;       // the source of the mail
    private String messageId;  // the message id in the mail header
    
    //----------------------------------------------------------------
    // Constructor
    //----------------------------------------------------------------
    /**
     * Constructor
     *
     * @param file the file that contains the mail
     * @throws IOException exception while reading the file
     */
    public MailFile(File file) throws IOException {
	this.file = file;
	readAttributes(file);
    }

    //----------------------------------------------------------------
    // Setters
    //----------------------------------------------------------------
    /**
     * Change the name of the file to the message id
     */
    public void updateFilename() throws IOException {
	if (messageId != null && !file.getName().equals(messageId)) {
	    File newFile = new File(file.getParent(), messageId);
	    if (newFile.exists()) {
		newFile.delete();
	    }
	    Files.move(file.toPath(), newFile.toPath());
	    this.file = newFile;
	}
    }
    
    //----------------------------------------------------------------
    // Getters
    //----------------------------------------------------------------
    /**
     * Give the source of the mail in the mail header
     *
     * @return the source of the mail
     */
    public String getFrom() {
	return this.from;
    }

    /**
     * Give the message id in the mail header
     *
     * @return the message id
     */
    public String getMessageId() {
	return this.messageId;
    }

    /**
     * Give the file that contains the mail
     *
     * @return the file
     */
    public File getFile() {
	return this.file;
    }
    
    //----------------------------------------------------------------
    // Private methods
    //----------------------------------------------------------------
    /**
     * Read the mail header to retrieve its attributes
     *
     * @param file the file that contains the mail
     * @throws IOException exception while reading the file
     */
    private void readAttributes(File file) throws IOException {
	try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	    boolean done = false;
	    String line = reader.readLine();
	    while (!done && line != null && line.length() > 0) {
		String[] tokens = line.split(":");
		if (tokens.length > 0) {
		    if (tokens[0].equalsIgnoreCase("From")) {
			from = getName(line.substring(6));
		    } else if (tokens[0].equalsIgnoreCase("Message-ID")) {
			messageId = getId(line.substring(12));
		    }
		    done = from != null && messageId != null;
		}
		line = reader.readLine();
	    }
	}
    }

    /**
     * Get the name in a string having the form "name@domain" or
     * "Name Surname <name@domain>"
     *
     * @param str input string
     * @return the name in the input string
     */
    private String getName(String str) {
	
	// extract the part between "<>"
	String name = getId(str);
	
	// extract the part before "@"
	int idx = name.indexOf("@");
	if (idx >= 0) {
	    return name.substring(0, idx);
	}
	return name;
    }

    /**
     * Extract the part of the input string between "<>"
     *
     * @param str input string
     * @return the substring between "<>"
     */
    private String getId(String str) {
	int idx = str.indexOf("<");
	// extract the part between <>
	if (idx >= 0) {
	    int idx2 = str.indexOf(">", idx+1);
	    if (idx2 > 0) {
		return str.substring(idx+1, idx2);
	    }
	}
	return str;
    }
}
