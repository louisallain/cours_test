/**
 * Bibliothèque de communication événementielle
 * @author F. Raimbault
 * @date septembre 2019
 */
package csp_V1;

/**
 * Etats de l'automate de synchronisation entre voisins
 */
enum SyncState{ // etat des échanges de messages READY/ACK avec un voisin 
  
  /** etat initial */
  NOTHING_SND_NOTHING_RCV, 
  /** prêt, rien reçu du voisin */
  READY_SND_NOTHING_RCV,
  /** rien envoyé, voisin prêt */
  NOTHING_SND_READY_RCV, 
  /** ACK envoyé, voisin prêt */
  ACK_SND_READY_RCV,
  /** voisin prêt et nous aussi */
  ACK_SND_ACK_RCV; 
  
  /**
   * Tag des messages de synchronisation
   */
  static final String SYNC_TAG="sync_tag";
  /**
   * Contenu d'un message de synchronisation informant un voisin qu'il est prêt
   */
  static final String SYNC_MSG_READY="msg_ready"; 
  /**
   * Contenu d'un message de synchronisation informant un voisin qu'il a reçu son message 
   * ready et donc que lui même est prêt  
   */
  static final String SYNC_MSG_ACK="msg_ack"; 
}