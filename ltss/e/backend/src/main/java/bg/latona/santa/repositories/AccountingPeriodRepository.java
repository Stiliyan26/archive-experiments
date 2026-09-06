package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.AccountingPeriod;
import bg.latona.santa.entities.selfie.QAccountingPeriod;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;


public interface AccountingPeriodRepository extends CommonRepository<AccountingPeriod, QAccountingPeriod, Long>, JpaSpecificationExecutor<AccountingPeriod> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	AccountingPeriod findFirstByMonthAndCompanyAndDeleted(String month, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<AccountingPeriod> findByIsActiveAndDeleted(boolean isActive, boolean deleted);
}
