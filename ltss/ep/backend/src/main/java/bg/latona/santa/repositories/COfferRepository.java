package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.COffer;
import bg.latona.santa.entities.santa.common.QCOffer;

public interface COfferRepository extends CommonRepository<COffer, QCOffer, Long> {

	@Query(value = "SELECT coalesce(max(numberOfr), 0) FROM COffer")
	Long getMaxAutoIncNum();
	
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<COffer> findByCompanyAndDeleted(ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	COffer findFirstByNumberOfrAndCompanyAndDeleted(Integer numberOfr, ManagedCompany company, boolean deleted);
}
