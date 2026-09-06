package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCcProgram;
import bg.latona.santa.entities.santa.finance.QFCcProgram;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCcProgramRepository extends CommonRepository<FCcProgram, QFCcProgram, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCcProgram findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}
