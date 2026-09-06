package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.offer.QOfferLine;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.offer.OfferLine;
import bg.latona.santa.entities.offer.OfferToClient;

public interface OfferLineRepository extends CommonRepository<OfferLine, QOfferLine, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	OfferLine findFirstByArticleAndOfferToClient(Article article, OfferToClient offerToClient);
}