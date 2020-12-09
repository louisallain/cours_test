/**
 * Implémentation distribuée d'une JavaSpace
 * Université de Bretagne Sud
 * Master 2 INFO 
 * @author F. Raimbault
 */
package dtspace;

import java.io.IOException;

/**
 * Une mémoire abstraite de tuples
 * La mémoire peut être locale, centralisée ou distribuée.
 */
public interface TupleMemory {

  /**
   * Crée une mémoire de tuples
   * @param size nombre de tuples attendus
   */
  void create(int size);
  
  /**
   * Ajout d'un tuple à la mémoire de tuple
   * @param tuple le tuple à ajouter
   */
  void write(Tuple tuple);

  /**
   * Lecture d'un tuple de la mémoire.
   * Bloquant tant qu'aucun tuple correspondant au motif n'est trouvé
   * @param m le motif du tuple recherché
   * @return un tuple qui correspond au motif 
   *         (ou null si la recherche est interrompue)
   */
  Tuple read(Tuple m);

  /**
   * Lecture d'un tuple de la mémoire.
   * Retourne immédiatement si aucun tuple correspondant au motif n'est trouvé.
   * @param m le motif du tuple recherché
   * @return un tuple qui correspond au motif ou null
   */
  Tuple readIfExists(Tuple m);

  /**
   * Extraction d'un tuple de la mémoire.
   * Bloquant tant qu'aucun tuple correspondant au motif n'est trouvé
   * @param m le motif du tuple recherché
   * @return un tuple qui correspond au motif 
   *        (ou null si la recherche est interrompue)
   */
  Tuple take(Tuple m);

  /**
   * Extraction d'un tuple de la mémoire.
   * Retourne immédiatement si aucun tuple correspondant au motif n'est trouvé
   * @param m le motif du tuple recherché
   * @return un tuple qui correspond au motif ou null si aucun tuple n'est trouvé
   */
  Tuple takeIfExists(Tuple m);

  /**
   * Vidage de la mémoire de tuples
   * @throws IOException 
   */
  void clean() throws IOException;

  /**
   * Taille de la mémoire de tuples
   * @return le nombre de tuples
   */
  int size();

  /**
   * Fermeture (libération) de la mémoire de tuples
   */
  void distroy();

  /**
   * Positionnement du drapeau d'affichage ou non des opérations en cours sur la mémoire de tuples
   * @param b  vrai pour afficher les traces, faux sinon
   */
  void setTrace(boolean b);
  
  /**
   * Dump du contenu de la mémoire de tuples
   * @return une chaine contenant la liste de tuples
   */
  String dump();
}

