package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedOrderRow;
import bg.latona.santa.entities.offer.OfferLine;
import bg.latona.santa.entities.wato.ImportedArticle;
import bg.latona.santa.entities.wato.ImportedOrder;
import bg.latona.santa.entities.wato.ImportedOrderRow;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.Nullable;

public interface ImportedOrderRowRepository extends CommonRepository<ImportedOrderRow, QImportedOrderRow, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrderRow> findByArticle(ImportedArticle article);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrderRow> findByForeignId(String foreignId);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrderRow> findByForeignArticleIdAndArticle(String foreignArticleId, @Nullable ImportedArticle article);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrderRow> findByForeignOrderIdAndOrder(String foreignOrderId, @Nullable ImportedOrder order);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrderRow> findByOrderAndArticleAndOfferLine(ImportedOrder order, ImportedArticle article, OfferLine offerLine);
}