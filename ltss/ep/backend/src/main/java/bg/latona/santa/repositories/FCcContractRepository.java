package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCcContract;
import bg.latona.santa.entities.santa.finance.QFCcContract;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCcContractRepository extends CommonRepository<FCcContract, QFCcContract, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCcContract findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}
