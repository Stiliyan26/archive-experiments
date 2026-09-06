package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FChartAccount;
import bg.latona.santa.entities.santa.finance.QFChartAccount;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FChartAccountRepository extends CommonRepository<FChartAccount, QFChartAccount, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FChartAccount findFirstByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted);
}