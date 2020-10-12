/**
 * Bibliothèque de communication évènementielle
 * @author F. Raimbault
 */

package election;

/**
 * Fonction de poids utilisée dans l'algorithme d'élection
 */
interface Weight {
  
  /**
   * Poids d'un processus
   * @return son poids
   */
  public double getValue();

  /**
   * Fixe le poids.
   * @param value le poids.
   */
  public void setValue(double value);
}
