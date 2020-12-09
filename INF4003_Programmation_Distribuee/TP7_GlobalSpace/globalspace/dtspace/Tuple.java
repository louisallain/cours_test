/**
 * Implémentation distribuée d'une JavaSpace
 * Université de Bretagne Sud
 * Master 2 INFO 
 * @author F. Raimbault
 */
package dtspace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Un tuple est une liste non vide de champs comparables et indexés.
 * Si l'un au moins d'un des champs vaut null, le tuple est un motif.
 */
public class Tuple implements Comparable<Tuple>, Serializable{
  
  /** tuple sans aucun champ */
  public static final Tuple EMPTY = new Tuple();
  
  /** tuple avec un seul champ qui est null : < ? > */
  // utile pour alléger l'appel à l'un des deux constructeurs possibles
  public static final Tuple NULL1 = new Tuple((new Comparable[]{null}));
  
  /** la liste des valeurs du tuple */
  private List<Comparable> list;
  
  /** noeud qui possède ce tuple (utile pour la meo distribuée) */
  private int node_id;
  
  /**
   * Création d'un tuple vide (une seule instance de ce tuple)
   */
  private Tuple(){
    list= new ArrayList();
  }
  
  /**
   * Création d'un tuple à partir d'un nombre quelconque de champs
   * @param fields les champs du tuple
   */
  public Tuple(Comparable...fields){
    this(Arrays.asList(fields));
  }
  
  /**
   * Création d'un tuple à partir de la liste de ses champs
   * @param fields les champs du tuple
   */
  public Tuple(List<Comparable> fields){
    list= new ArrayList(fields);
  }
  
  /**
   * Accès à un champ d'un tuple
   * @param index numéro du champ concerné
   * @return la valeur du champ de numéro index
   */
  public Comparable getField(int index){
    return list.get(index);
  }
  
  /**
   * Modification d'un champ d'un tuple
   * @param index numéro du champ concerné
   * @param field la nouvelle valeur du champ de numéro index
   */
  public void setField(int index, Comparable field){
    list.set(index,field);
  }
  /**
   * Mise à jour de l'identifiant du noeud qui possède ce tuple
   * @param id identifiant du noeud qui possède ce tuple
   */
  public void setNodeId(int id){
    node_id= id;
  }
  /**
   * Numéro du noeud qui possède ce tuple 
   * @return identifiant du noeud qui possède ce tuple 
   */
  public int getNodeID(){
    return node_id;
  }
  /**
   * La taille d'un tuple
   * @return le nombre de champ du tuple
   */
  public int size(){
    return list.size();
  }
  /**
   * Prédicat de tuple vide
   * @return vrai ssi le tuple est vide, ie. ne contient aucun champs
   */
  public boolean isEmpty(){
    return list.isEmpty();
  }
  /**
   * Test de correspondance entre un tuple et un motif
   * @param m le motif auquel est comparé ce tuple
   * @return 1 si ce tuple correspond au motif, 0 sinon
   */
  @Override
  public int compareTo(Tuple m) {
    if (this.size() != m.size()) return 0;
    for(int i=0, j=0; i<this.size(); i++, j++){
      Comparable f1= this.getField(i);
      if (f1==null) continue;
      Comparable f2= m.getField(j);
      if (f2==null) continue;
      if (f1.getClass() != f2.getClass()) return 0;
      if (f1.compareTo(f2)!=0) return 0;
    }
    return 1;
  }

  /**
   * Liste des valeurs de chaque champ du tuple
   * @return la liste des champs
   */
  public List<Comparable> getFields() {
    
    return list;
  }

  /**
   * Serialisation d'un tuple
   * @param t le tuple 
   * @return le tableau de bytes contenant le résultat de sa sérialisation. 
   *         null en cas d'erreur
   */
  public static byte[] toBytes(Tuple t){
    ByteArrayOutputStream os= new ByteArrayOutputStream(1024);
    try {
      ObjectOutputStream oos= new ObjectOutputStream(os);
      oos.writeObject(t.list);
    } catch (IOException e) {
      return null;
    }
    return os.toByteArray();
  }

  /**
   * Désérialisation d'un tuple
   * @param bytes un tableau de bytes contenant un tuple sérialisé
   * @return le tuple après désérialisation. null en cas d'erreur.
   */
  public static Tuple fromBytes(byte[] bytes){
    ByteArrayInputStream fis = new ByteArrayInputStream(bytes);
    List fields;
    try {
      ObjectInputStream ois = new ObjectInputStream(fis);
      fields = (ArrayList) ois.readObject();
    } catch (ClassNotFoundException e) {
     return null;
    } catch (IOException e) {
      return null;
    }
    return (fields.size()==0) ? Tuple.EMPTY : new Tuple(fields);
  }
  
  /**
   * Chaîne représentant textuellement le contenu d'un tuple,
   * sous la forme <champ1,champ2,...>
   * La méthode toString() est invoqué sur chaque champ
   * @see java.lang.Object#toString()
   */
  public String toString(){
    if (list.isEmpty()) return "<EMPTY>";
    StringBuilder sb= new StringBuilder("<");
    for(Object f:list){
      sb.append((f!=null)? f.toString():"?").append(',');
    }
    if (! list.isEmpty()) sb.delete(sb.length()-1, sb.length()); // enlève la dernière ","
    return sb.append('>').toString();
  }

}
