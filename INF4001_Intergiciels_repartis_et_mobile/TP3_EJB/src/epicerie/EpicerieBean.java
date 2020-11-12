package epicerie;

import javax.ejb.*;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@Remote(Epicerie.class)
public class EpicerieBean implements Epicerie {

    @PersistenceContext
    private EntityManager em;

    /**
    * Donne tous les articles.
    */
    public List<Article> listeArticles() {
        return em.createQuery("SELECT a FROM Article a").getResultList();
    }
    /**
    * Créer un nouvel article.
    */
    public void creerArticle(Article article) {
        em.persist(article);
    }
    /**
    * Solde un article.
    * @param code le code de l'article à solder
    * @param pourcentageRemise le pourcentage de remise compris entre 0 et 1
    */
    public void solderArticle(int code, int pourcentageRemise) {
        Article art = em.find(Article.class, code);
        art.setPrix(art.getPrix() * pourcentageRemise);
    }
}