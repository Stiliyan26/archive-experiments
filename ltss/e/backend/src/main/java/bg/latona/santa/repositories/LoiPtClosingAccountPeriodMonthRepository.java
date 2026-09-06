package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.finance.LoiPtClosingAccountPeriodMonth;
import bg.latona.santa.entities.santa.finance.QLoiPtClosingAccountPeriodMonth;
import org.springframework.data.rest.core.annotation.RestResource;

public interface LoiPtClosingAccountPeriodMonthRepository extends CommonRepository<LoiPtClosingAccountPeriodMonth, QLoiPtClosingAccountPeriodMonth, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	LoiPtClosingAccountPeriodMonth findFirstByListOptionItemCodeAndCompanyAndDeleted(Long listOptionItemCode, ManagedCompany company, boolean deleted);
}
