package bg.latona.santa.repositories;

import bg.latona.santa.entities.wato.QImportedOrder;
import bg.latona.santa.entities.offer.OfferToClient;
import bg.latona.santa.entities.wato.ImportedOrder;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.Nullable;

public interface ImportedOrderRepository extends CommonRepository<ImportedOrder, QImportedOrder, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrder> findByForeignId(String foreignId);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrder> findByForeignIdAndOffer(String foreignId, @Nullable OfferToClient offer);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ImportedOrder> findByOffer(OfferToClient offer);
}