package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCcReserve1;
import bg.latona.santa.entities.santa.finance.QFCcReserve1;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCcReserve1Repository extends CommonRepository<FCcReserve1, QFCcReserve1, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCcReserve1 findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}
