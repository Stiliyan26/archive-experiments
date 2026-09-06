package bg.latona.santa.repositories;

import bg.latona.santa.entities.article.ArticleService;
import bg.latona.santa.entities.article.QArticleService;


public interface ArticleServiceRepository extends CommonRepository<ArticleService, QArticleService, Long> {
	
}