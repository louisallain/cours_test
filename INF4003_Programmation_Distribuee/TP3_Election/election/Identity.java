/**
 * Bibliothèque de communication évènementielle
 * @author F. Raimbault
 */

package election;

/**
 * Fonction de poids déterminée par l'identité du processus
 */
public class Identity implements Weight {

  double weight;
  
  /**
   * Identifiant du processus dans le réseau
   */
  public Identity(int id) {
    this.weight= id;
  }

  /**
   * @see election.Weight#getValue()
   */
  @Override
  public double getValue() {
    return weight;
  }

  @Override
  public void setValue(double value) {
    this.weight = value;
  }

}
