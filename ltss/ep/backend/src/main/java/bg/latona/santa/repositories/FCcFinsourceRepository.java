package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCcFinsource;
import bg.latona.santa.entities.santa.finance.QFCcFinsource;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCcFinsourceRepository extends CommonRepository<FCcFinsource, QFCcFinsource, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCcFinsource findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}
