package epicerie;

import java.util.List;

public interface Epicerie {

    /**
    * Donne tous les articles.
    */
    public List<Article> listeArticles();
    /**
    * Créer un nouvel article.
    */
    public void creerArticle(Article article);
    /**
    * Solde un article.
    * @param code le code de l'article à solder
    * @param pourcentageRemise le pourcentage de remise compris entre 0 et 1
    */
    public void solderArticle(int code, int pourcentageRemise);
}