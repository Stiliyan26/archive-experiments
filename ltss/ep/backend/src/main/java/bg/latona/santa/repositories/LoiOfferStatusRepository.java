package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.LoiOfferStatus;
import bg.latona.santa.entities.santa.common.QLoiOfferStatus;

public interface LoiOfferStatusRepository extends CommonRepository<LoiOfferStatus, QLoiOfferStatus, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiOfferStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
