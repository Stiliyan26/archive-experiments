package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCcReserve2;
import bg.latona.santa.entities.santa.finance.QFCcReserve2;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCcReserve2Repository extends CommonRepository<FCcReserve2, QFCcReserve2, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCcReserve2 findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}
