package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.offer.QOfferToClient;
import bg.latona.santa.entities.offer.OfferToClient;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

public interface OfferToClientRepository extends CommonRepository<OfferToClient, QOfferToClient, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<OfferToClient> findByOfferCode(String offerCode);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<OfferToClient> findByForeignId(String foreignId);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<OfferToClient> findByOriginalOffer(OfferToClient originalOffer);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	OfferToClient findFirstByOfferCodeAndCompanyAndDeleted(String offerCode, ManagedCompany company, boolean deleted);
}