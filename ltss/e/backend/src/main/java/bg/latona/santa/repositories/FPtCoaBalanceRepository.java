package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.*;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FPtCoaBalanceRepository extends CommonRepository<FPtCoaBalance, QFPtCoaBalance, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FPtCoaBalance findFirstByCoaIdAndOutCodeAndPeriodAndCompanyAndDeleted(FChartAccount coaId, CCcOrganizationUnit outCode, String period, ManagedCompany company, boolean deleted);
}