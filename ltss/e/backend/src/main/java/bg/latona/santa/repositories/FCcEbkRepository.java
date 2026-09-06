package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCcEbk;
import bg.latona.santa.entities.santa.finance.QFCcEbk;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCcEbkRepository extends CommonRepository<FCcEbk, QFCcEbk, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCcEbk findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}
