package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.FPtClosingAccount;
import bg.latona.santa.entities.santa.finance.LoiPtClosingAccountPeriodMonth;
import bg.latona.santa.entities.santa.finance.QFPtClosingAccount;
import org.springframework.data.rest.core.annotation.RestResource;

public interface FPtClosingAccountRepository extends CommonRepository<FPtClosingAccount, QFPtClosingAccount, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FPtClosingAccount findFirstByOutCodeAndPeriodMonthAndPeriodYearAndCompanyAndDeleted(CCcOrganizationUnit outCode, LoiPtClosingAccountPeriodMonth periodMonth, String periodYear, ManagedCompany company, boolean deleted);
}


