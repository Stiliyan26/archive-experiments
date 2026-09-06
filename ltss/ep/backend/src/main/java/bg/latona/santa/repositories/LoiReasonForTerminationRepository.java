package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.QLoiReasonForTermination;
import bg.latona.santa.entities.selfie.LoiReasonForTermination;

import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiReasonForTerminationRepository extends CommonRepository<LoiReasonForTermination, QLoiReasonForTermination, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiReasonForTermination findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
