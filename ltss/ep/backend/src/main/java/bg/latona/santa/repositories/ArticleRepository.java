package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.article.QArticle;

public interface ArticleRepository extends CommonRepository<Article, QArticle, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Article findFirstByForeignId(String foreignId);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Article findFirstByNameAndCompanyAndDeleted(String name, ManagedCompany company, boolean deleted);
}