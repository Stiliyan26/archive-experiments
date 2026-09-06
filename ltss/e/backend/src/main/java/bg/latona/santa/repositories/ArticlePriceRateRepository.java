package bg.latona.santa.repositories;

import bg.latona.santa.entities.article.QArticlePriceRate;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.article.ArticlePriceRate;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

public interface ArticlePriceRateRepository extends CommonRepository<ArticlePriceRate, QArticlePriceRate, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ArticlePriceRate> findByArticle(@Param("article") Article article);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ArticlePriceRate> findByVendorAndArticle(String vendor, Article article);
}