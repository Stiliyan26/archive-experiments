package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FCcFunction;
import bg.latona.santa.entities.santa.finance.QFCcFunction;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FCcFunctionRepository extends CommonRepository<FCcFunction, QFCcFunction, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCcFunction findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}
