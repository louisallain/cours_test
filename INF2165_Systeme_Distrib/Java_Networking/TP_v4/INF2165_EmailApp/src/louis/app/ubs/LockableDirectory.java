package louis.app.ubs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A directory that can be locked/unlocked by checking the existence
 * of a .lock empty file
 */ 
public class LockableDirectory {

    //---------------------------------------------------------------
    private boolean locked;         // true if this directory is locked
    private File directory;         // the directory in the file system
    
    //---------------------------------------------------------------
    /**
     * Constructor
     *
     * @param directory the directory to be locked/unlocked
     * @throws FileNotFoundException if the given directory does not exist
     */
    public LockableDirectory(File directory) throws FileNotFoundException {

	if (!directory.isDirectory()) {
	    throw new FileNotFoundException(directory.getPath() +
					    " is not a directory");
	}
	this.directory = directory;
    }

    //---------------------------------------------------------------
    /**
     * Give the directory in the file system
     *
     * @return the directory in the file system
     */
    public File getDirectory() {
	return directory;
    }

    //---------------------------------------------------------------
    /**
     * Wait until a lock is acquired on the directory
     */
    public void waitForLock() {

	while (!acquireLock()) {
	    try {
		Thread.sleep(100);
	    } catch (InterruptedException e) {
		System.err.println(e.getClass().getName() + ": " +
				   e.getMessage() + ". " +
				   "This should not occur");
	    }
	}
    }

    //---------------------------------------------------------------
    /**
     * Tries to acquire a lock on the directory
     *
     * @return true if the lock has been acquired
     */
    public synchronized boolean acquireLock() {
	
	if (!locked) {
	    
	    File lockFile = new File(directory, ".lock");
	    try {
		this.locked = lockFile.createNewFile();
	    } catch (IOException e) {
		System.err.println("Unable to create " +
				   lockFile.getPath() +": " + 
				   e.getClass().getName());
	    }
	}
	return locked;
    }

    //---------------------------------------------------------------
    /**
     * Try to release the lock on the directory
     *
     * @return true if the lock has been released
     */
    public synchronized boolean releaseLock() {
	File lockFile = new File(directory, ".lock");
	boolean unlocked = !lockFile.exists();
	if (!unlocked) {
	    unlocked = lockFile.delete();
	}
	this.locked = !unlocked;
	return unlocked;
    }

    //---------------------------------------------------------------
    /**
     * Return true if the lock has been acquired by this object
     *
     * @return true if the lock has been acquired
     */
    public synchronized boolean hasLock() {
	return locked;
    }
}
