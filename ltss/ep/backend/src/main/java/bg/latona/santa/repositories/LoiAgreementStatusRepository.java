package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.LoiAgreementStatus;
import bg.latona.santa.entities.selfie.QLoiAgreementStatus;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

public interface LoiAgreementStatusRepository extends CommonRepository<LoiAgreementStatus, QLoiAgreementStatus, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiAgreementStatus findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);


	@RestResource(exported = false)
	List<LoiAgreementStatus> findByListOptionItemCodeInAndCompanyAndDeleted(
			List<Long> statusCodes, ManagedCompany managedCompany, boolean deleted
	);
}
