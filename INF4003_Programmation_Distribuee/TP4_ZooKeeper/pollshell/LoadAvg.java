/**
 * Bibliothèque de communication évènementielle
 * @author F. Raimbault
 */

package pollshell;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

/**
 * Lecture du fichier loadavg pour déterminer le poids d'un processus dans une élection
 */
public class LoadAvg {

  private double loadAvgOfLastMinut;
  /**
   * Accès au fichier /proc/loadavg et mémorisation de la valeur du dernier ID
   */
  public LoadAvg() {
    try {
      final String filename="/proc/loadavg";
      @SuppressWarnings("resource")
      final Scanner scanner = new Scanner(new File(filename)).useLocale(Locale.ENGLISH);
      this.loadAvgOfLastMinut = scanner.nextDouble(); // loadavg of the last minute period
      scanner.nextDouble(); // loadavg of the last five minute period
      scanner.nextDouble(); // loadavg of the last ten minute period
      scanner.next(); // running processes / total number of processes
      scanner.nextInt(); // last process ID
      scanner.close();
    } catch (FileNotFoundException e) {
      System.err.println(e);
    }
  }

  public double getLoadAvgOfLastMinut() {
    return this.loadAvgOfLastMinut;
  }
}
