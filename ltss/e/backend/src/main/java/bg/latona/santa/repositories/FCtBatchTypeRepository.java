package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.FCtBatchType;
import bg.latona.santa.entities.santa.finance.QFCtBatchType;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtBatchTypeRepository extends CommonRepository<FCtBatchType, QFCtBatchType, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtBatchType findFirstByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted);
}
