package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CDelivery;
import bg.latona.santa.entities.santa.common.QCDelivery;

public interface CDeliveryRepository extends CommonRepository<CDelivery, QCDelivery, Long>{

	@Query(value = "SELECT coalesce(max(deyNumber), 0) FROM CDelivery")
	Long getMaxAutoIncNum();
	
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CDelivery> findFirstByCompanyAndDeleted(ManagedCompany company, boolean deleted);
}
