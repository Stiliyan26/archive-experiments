package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CSale;
import bg.latona.santa.entities.santa.common.QCSale;

public interface CSaleRepository extends CommonRepository<CSale, QCSale, Long> {

	@Query(value = "SELECT coalesce(max(documentNumber), 0) FROM CSale")
	Long getMaxAutoIncNum();
	
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CSale> findCSaleByCompanyAndDeleted(ManagedCompany company, boolean deleted);
}
