/**
 * Bibliothèque de communication évènementielle
 * @author F. Raimbault
 */

package election;

import java.util.LinkedList;
import java.util.List;

/**
 * Mise en oeuvre du quadruplet de l'algorithme d'élection. 
 * Fonctions de sérialisation et de désérialisation d'un quadruplet dans une chaîne.
 */
class Quadruplet {

  /**
   * Identité du site qui a lancé l'exploration
   */
  int root;
  /**
   * Poids du site qui a lancé l'exploration
   */
  double weight;
  /**
   * Ensemble des identités des sites visités
   */
  List<Integer> done_list;
  /**
   * Ensemble des identités des voisins immédiats des sites de done_list qui n'ont pas 
   * encore été atteints
   */
  List<Integer> todo_list;
  
  /**
   * Construction d'un quadruplet
   */
  Quadruplet(int root, double weight, List<Integer> done_list, List<Integer> todo_list) {
    
    this.root= root;
    this.weight= weight;
    this.done_list= done_list;
    this.todo_list= todo_list;
  }

  /**
   * Sérialisation d'un quadruplet en contenu d'un message
   */
  public String toMessageContent(){
    StringBuilder sb= new StringBuilder();
    sb.append(root).append(":");
    sb.append(weight).append(":");
    if (done_list.isEmpty()) sb.append(",");
    else{
      for(int done_id:done_list){
        sb.append(done_id).append(",");
      }
    }
    sb.append(":");
    if (todo_list.isEmpty()) sb.append(",");
    else{
      for(int todo_id:todo_list){
        sb.append(todo_id).append(",");
      }
    }
    return sb.toString();
  }
  
  /**
   * Désérialisation d'un quadruplet à partir du contenu d'un message
   * 
   * @param content contenu d'un message
   * @return le quadruplet extrait du contenu du message (null en cas d'erreur de désérialisation)
   */
  static Quadruplet fromMessageContent(String content){
    try {
      String[] parts= content.split(":");
      if (parts==null || parts.length != 4) return null;
      int root= Integer.parseInt(parts[0]);
      double weight= Double.parseDouble(parts[1]);
      List<Integer> done_list= new LinkedList<Integer>();
      String [] done_parts= parts[2].split(",");
      for(String s:done_parts){
          done_list.add(Integer.parseInt(s));
      }
      List<Integer> todo_list= new LinkedList<Integer>();
      String [] todo_parts= parts[3].split(",");
      for(String s:todo_parts){
          todo_list.add(Integer.parseInt(s));
      }
      return new Quadruplet(root, weight, done_list, todo_list);
    } catch (NumberFormatException e) {
      System.err.println(e.toString());
      return null;
    }
  }
  
  /**
   * Représentation d'un quadruplet sous la forme <root,weight,(done_list),(todo_list)>
   * @see java.lang.Object#toString()
   */
  public String toString(){
    StringBuilder sb= new StringBuilder("<");
    sb.append(root).append(":");
    sb.append(weight).append(":(");
    for(int done_id:done_list){
      sb.append(done_id).append(",");
    }
    if (! done_list.isEmpty()) sb.deleteCharAt(sb.length()-1); // élimine la "," inutile
    sb.append("):(");
    for(int todo_id:todo_list){
      sb.append(todo_id).append(",");
    }
    if (! todo_list.isEmpty()) sb.deleteCharAt(sb.length()-1); // élimine la "," inutile
    return sb.append(")>").toString();
  }

}
