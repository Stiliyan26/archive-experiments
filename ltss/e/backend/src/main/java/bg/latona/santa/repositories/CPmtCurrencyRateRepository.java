package bg.latona.santa.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import bg.latona.santa.entities.santa.common.CPmtCurrencyRate;
import bg.latona.santa.entities.santa.common.QCPmtCurrencyRate;

public interface CPmtCurrencyRateRepository extends CommonRepository<CPmtCurrencyRate, QCPmtCurrencyRate, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CPmtCurrencyRate> findByCuyCodeAndOutCodeAndCompanyAndDeleted(CCtCurrency cuyCode, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CPmtCurrencyRate findFirstByCuyCodeAndDateFromLessThanEqualAndDateToGreaterThanEqualAndCompanyAndDeleted(CCtCurrency p_from_cur,
			LocalDate p_date, LocalDate p_date2, ManagedCompany company, boolean deleted);
}
